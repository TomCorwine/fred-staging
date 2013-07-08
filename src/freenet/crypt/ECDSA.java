package freenet.crypt;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.security.SignatureException;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import freenet.node.FSParseException;
import freenet.support.Base64;
import freenet.support.Logger;
import freenet.support.SimpleFieldSet;

public class ECDSA {
    public final Curves curve;
    private final KeyPair key;

    public enum Curves {
        // rfc5903 or rfc6460: it's NIST's random/prime curves : suite B
        // Order matters. Append to the list, do not re-order.
        P256("secp256r1", "SHA256withECDSA", 91, 72),
        P384("secp384r1", "SHA384withECDSA", 120, 104),
        P521("secp521r1", "SHA512withECDSA", 158, 139);
        
        public final ECGenParameterSpec spec;
        private final KeyPairGenerator keygen;
        /** The hash algorithm used to generate the signature */
        public final String defaultHashAlgorithm;
        /** Expected size of a DER encoded pubkey in bytes */
        public final int modulusSize;
        /** Maximum (padded) size of a DER-encoded signature (network-format) */
        public final int maxSigSize;
        
        private Curves(String name, String defaultHashAlgorithm, int modulusSize, int maxSigSize) {
            this.spec = new ECGenParameterSpec(name);
            KeyPairGenerator kg = null;
            try {
                kg = KeyPairGenerator.getInstance("EC");
                kg.initialize(spec);
            } catch (NoSuchAlgorithmException e) {
                Logger.error(ECDSA.class, "NoSuchAlgorithmException : "+e.getMessage(),e);
                e.printStackTrace();
            } catch (InvalidAlgorithmParameterException e) {
                Logger.error(ECDSA.class, "InvalidAlgorithmParameterException : "+e.getMessage(),e);
                e.printStackTrace();
            }
            this.keygen = kg;
            this.defaultHashAlgorithm = defaultHashAlgorithm;
            this.modulusSize = modulusSize;
            this.maxSigSize = maxSigSize;
        }
        
        public synchronized KeyPair generateKeyPair() {
            return keygen.generateKeyPair();
        }
        
        public SimpleFieldSet getSFS(ECPublicKey pub) {
            SimpleFieldSet ecdsaSFS = new SimpleFieldSet(true);
            SimpleFieldSet curveSFS = new SimpleFieldSet(true);
            curveSFS.putSingle("pub", Base64.encode(pub.getEncoded()));
            ecdsaSFS.put(name(), curveSFS);
            return ecdsaSFS;
        }
        
        public String toString() {
            return spec.getName();
        }
    }
    
    /**
     * Initialize the ECDSA object: this will draw some entropy
     * @param curve
     */
    public ECDSA(Curves curve) {
        this.curve = curve;
        this.key = curve.keygen.generateKeyPair();
    }
    
    /**
     * Initialize the ECDSA object: from an SFS generated by asFieldSet()
     * @param curve
     * @throws FSParseException 
     */
    public ECDSA(SimpleFieldSet sfs, Curves curve) throws FSParseException {
        byte[] pub = null;
        byte[] pri = null;
        try {
            pub = Base64.decode(sfs.get("pub"));
            if (pub.length > curve.modulusSize)
                throw new InvalidKeyException();
            ECPublicKey pubK = getPublicKey(pub);

            pri = Base64.decode(sfs.get("pri"));
            PKCS8EncodedKeySpec ks = new PKCS8EncodedKeySpec(pri);
            KeyFactory kf = KeyFactory.getInstance("ECDSA");
            ECPrivateKey privK = (ECPrivateKey) kf.generatePrivate(ks);

            this.key = new KeyPair(pubK, privK);
        } catch (Exception e) {
            throw new FSParseException(e);
        }
        this.curve = curve;
    }
    
    public byte[] sign(byte[] data) {
        return sign(data, 0, data.length);
    }

    public byte[] sign(byte[] data, int offset, int len) {
        if(data.length == 0 || data.length < len)
            return null;
        byte[] result = null;
        try {
            while(true) {
                Signature sig = Signature.getInstance(curve.defaultHashAlgorithm);
                sig.initSign(key.getPrivate());
                sig.update(data, offset, len);
                result = sig.sign();
                // It's a DER encoded signature, most sigs will fit in N bytes
                // If it doesn't let's re-sign.
                if(result.length <= curve.maxSigSize)
                	break;
                else
                	Logger.error(this, "DER encoded signature used "+result.length+" bytes, more than expected "+curve.maxSigSize+" - re-signing...");
            }
        } catch (NoSuchAlgorithmException e) {
            Logger.error(this, "NoSuchAlgorithmException : "+e.getMessage(),e);
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            Logger.error(this, "InvalidKeyException : "+e.getMessage(),e);
            e.printStackTrace();
        } catch (SignatureException e) {
            Logger.error(this, "SignatureException : "+e.getMessage(),e);
            e.printStackTrace();
        }
        
        return result;
    }
    
    /**
     * Sign data and return a fixed size signature. The data does not need to be hashed, the 
     * signing code will handle that for us, using an algorithm appropriate for the keysize.
     * @return A zero padded DER signature (maxSigSize). Space Inefficient but constant-size.
     */
    public byte[] signToNetworkFormat(byte[] data, int offset, int len) {
        byte[] plainsig = sign(data, offset, len);
        int targetLength = curve.maxSigSize;

        if(plainsig.length != targetLength) {
            byte[] newData = new byte[targetLength];
            if(plainsig.length < targetLength) {
                System.arraycopy(plainsig, 0, newData, 0, plainsig.length);
            } else {
                throw new IllegalStateException("Too long!");
            }
            plainsig = newData;
        }
        return plainsig;
    }
    
    public boolean verify(byte[] signature, byte[] data) {
        return verify(curve, getPublicKey(), signature, 0, signature.length, data, 0, data.length);
    }
    
    public boolean verify(byte[] signature, int sigoffset, int siglen, byte[] data, int offset, int len) {
        return verify(curve, getPublicKey(), signature, sigoffset, siglen, data, offset, len);
    }
    
    public static boolean verify(Curves curve, ECPublicKey key, byte[] signature, byte[] data) {
        return verify(curve, key, signature, 0, signature.length, data, 0, data.length);
    }
    
    public static boolean verify(Curves curve, ECPublicKey key, byte[] signature, int sigoffset, int siglen, byte[] data, int offset, int len) {
        if(key == null || curve == null || signature == null || data == null)
            return false;
        boolean result = false;
        try {
            Signature sig = Signature.getInstance(curve.defaultHashAlgorithm);
            sig.initVerify(key);
            sig.update(data, offset, len);
            result = sig.verify(signature, sigoffset, siglen);
        } catch (NoSuchAlgorithmException e) {
            Logger.error(ECDSA.class, "NoSuchAlgorithmException : "+e.getMessage(),e);
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            Logger.error(ECDSA.class, "InvalidKeyException : "+e.getMessage(),e);
            e.printStackTrace();
        } catch (SignatureException e) {
            Logger.error(ECDSA.class, "SignatureException : "+e.getMessage(),e);
            e.printStackTrace();
        }
        return result;
    }
    
    public ECPublicKey getPublicKey() {
        return (ECPublicKey) key.getPublic();
    }
    
    /**
     * Returns an ECPublicKey from bytes obtained using ECPublicKey.getEncoded()
     * @param data
     * @return ECPublicKey or null if it fails
     */
    public static ECPublicKey getPublicKey(byte[] data) {
        ECPublicKey remotePublicKey = null;
        try {
            X509EncodedKeySpec ks = new X509EncodedKeySpec(data);
            KeyFactory kf = KeyFactory.getInstance("ECDSA");
            remotePublicKey = (ECPublicKey)kf.generatePublic(ks);
            
        } catch (NoSuchAlgorithmException e) {
            Logger.error(ECDSA.class, "NoSuchAlgorithmException : "+e.getMessage(),e);
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            Logger.error(ECDSA.class, "InvalidKeySpecException : "+e.getMessage(), e);
            e.printStackTrace();
        }
        
        return remotePublicKey;
    }
    
    /**
     * Returns an SFS containing:
     *  - the private key
     *  - the public key
     *  - the name of the curve in use
     *  
     *  It should only be used in NodeCrypto
     * @param includePrivate - include the (secret) private key
     * @return SimpleFieldSet
     */
    public SimpleFieldSet asFieldSet(boolean includePrivate) {
        SimpleFieldSet fs = new SimpleFieldSet(true);
        SimpleFieldSet fsCurve = new SimpleFieldSet(true);
        fsCurve.putSingle("pub", Base64.encode(key.getPublic().getEncoded()));
        if(includePrivate)
            fsCurve.putSingle("pri", Base64.encode(key.getPrivate().getEncoded()));
        fs.put(curve.name(), fsCurve);
        return fs;
    }
}
