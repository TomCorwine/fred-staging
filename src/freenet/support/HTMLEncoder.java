/*
  HTMLEncoder.java / Freenet
  Copyright (C) Yves Lempereur
  Copyright (C) 2005-2006 The Free Network project
  This program is free software; you can redistribute it and/or
  modify it under the terms of the GNU General Public License as
  published by the Free Software Foundation; either version 2 of
  the License, or (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
*/

package freenet.support;

import java.util.HashMap;

/**
 * Originally from com.websiteasp.ox pasckage.
 * 
 * Author: Yves Lempereur
 */
public class HTMLEncoder {
	public final static HashMap charTable;

	public static String encode(String s) {
		int n = s.length();
		StringBuffer sb = new StringBuffer(n);
		for (int i = 0; i < n; i++) {
			char c = s.charAt(i);
			if(charTable.containsKey(new Character(c))){
				sb.append('&'+(String)charTable.get(new Character(c))+';');
			}else
				sb.append(c);
		}
		return sb.toString();
	}
	
	static {
		charTable = new HashMap();
		charTable.put(new Character((char)34), "quot");
		charTable.put(new Character((char)38), "amp");
		charTable.put(new Character((char)39), "apos");
		charTable.put(new Character((char)60), "lt");
		charTable.put(new Character((char)62), "gt");
		charTable.put(new Character((char)160), "nbsp");
		charTable.put(new Character((char)161), "iexcl");
		charTable.put(new Character((char)162), "cent");
		charTable.put(new Character((char)163), "pound");
		charTable.put(new Character((char)164), "curren");
		charTable.put(new Character((char)165), "yen");
		charTable.put(new Character((char)166), "brvbar");
		charTable.put(new Character((char)167), "sect");
		charTable.put(new Character((char)168), "uml");
		charTable.put(new Character((char)169), "copy");
		charTable.put(new Character((char)170), "ordf");
		charTable.put(new Character((char)171), "laquo");
		charTable.put(new Character((char)172), "not");
		charTable.put(new Character((char)173), "shy");
		charTable.put(new Character((char)174), "reg");
		charTable.put(new Character((char)175), "macr");
		charTable.put(new Character((char)176), "deg");
		charTable.put(new Character((char)177), "plusmn");
		charTable.put(new Character((char)178), "sup2");
		charTable.put(new Character((char)179), "sup3");
		charTable.put(new Character((char)180), "acute");
		charTable.put(new Character((char)181), "micro");
		charTable.put(new Character((char)182), "para");
		charTable.put(new Character((char)183), "middot");
		charTable.put(new Character((char)184), "cedil");
		charTable.put(new Character((char)185), "sup1");
		charTable.put(new Character((char)186), "ordm");
		charTable.put(new Character((char)187), "raquo");
		charTable.put(new Character((char)188), "frac14");
		charTable.put(new Character((char)189), "frac12");
		charTable.put(new Character((char)190), "frac34");
		charTable.put(new Character((char)191), "iquest");
		charTable.put(new Character((char)192), "Agrave");
		charTable.put(new Character((char)193), "Aacute");
		charTable.put(new Character((char)194), "Acirc");
		charTable.put(new Character((char)195), "Atilde");
		charTable.put(new Character((char)196), "Auml");
		charTable.put(new Character((char)197), "Aring");
		charTable.put(new Character((char)198), "AElig");
		charTable.put(new Character((char)199), "Ccedil");
		charTable.put(new Character((char)200), "Egrave");
		charTable.put(new Character((char)201), "Eacute");
		charTable.put(new Character((char)202), "Ecirc");
		charTable.put(new Character((char)203), "Euml");
		charTable.put(new Character((char)204), "Igrave");
		charTable.put(new Character((char)205), "Iacute");
		charTable.put(new Character((char)206), "Icirc");
		charTable.put(new Character((char)207), "Iuml");
		charTable.put(new Character((char)208), "ETH");
		charTable.put(new Character((char)209), "Ntilde");
		charTable.put(new Character((char)210), "Ograve");
		charTable.put(new Character((char)211), "Oacute");
		charTable.put(new Character((char)212), "Ocirc");
		charTable.put(new Character((char)213), "Otilde");
		charTable.put(new Character((char)214), "Ouml");
		charTable.put(new Character((char)215), "times");
		charTable.put(new Character((char)216), "Oslash");
		charTable.put(new Character((char)217), "Ugrave");
		charTable.put(new Character((char)218), "Uacute");
		charTable.put(new Character((char)219), "Ucirc");
		charTable.put(new Character((char)220), "Uuml");
		charTable.put(new Character((char)221), "Yacute");
		charTable.put(new Character((char)222), "THORN");
		charTable.put(new Character((char)223), "szlig");
		charTable.put(new Character((char)224), "agrave");
		charTable.put(new Character((char)225), "aacute");
		charTable.put(new Character((char)226), "acirc");
		charTable.put(new Character((char)227), "atilde");
		charTable.put(new Character((char)228), "auml");
		charTable.put(new Character((char)229), "aring");
		charTable.put(new Character((char)230), "aelig");
		charTable.put(new Character((char)231), "ccedil");
		charTable.put(new Character((char)232), "egrave");
		charTable.put(new Character((char)233), "eacute");
		charTable.put(new Character((char)234), "ecirc");
		charTable.put(new Character((char)235), "euml");
		charTable.put(new Character((char)236), "igrave");
		charTable.put(new Character((char)237), "iacute");
		charTable.put(new Character((char)238), "icirc");
		charTable.put(new Character((char)239), "iuml");
		charTable.put(new Character((char)240), "eth");
		charTable.put(new Character((char)241), "ntilde");
		charTable.put(new Character((char)242), "ograve");
		charTable.put(new Character((char)243), "oacute");
		charTable.put(new Character((char)244), "ocirc");
		charTable.put(new Character((char)245), "otilde");
		charTable.put(new Character((char)246), "ouml");
		charTable.put(new Character((char)247), "divide");
		charTable.put(new Character((char)248), "oslash");
		charTable.put(new Character((char)249), "ugrave");
		charTable.put(new Character((char)250), "uacute");
		charTable.put(new Character((char)251), "ucirc");
		charTable.put(new Character((char)252), "uuml");
		charTable.put(new Character((char)253), "yacute");
		charTable.put(new Character((char)254), "thorn");
		charTable.put(new Character((char)255), "yuml");
		charTable.put(new Character((char)338), "OElig");
		charTable.put(new Character((char)339), "oelig");
		charTable.put(new Character((char)352), "Scaron");
		charTable.put(new Character((char)353), "scaron");
		charTable.put(new Character((char)376), "Yuml");
		charTable.put(new Character((char)402), "fnof");
		charTable.put(new Character((char)710), "circ");
		charTable.put(new Character((char)732), "tilde");
		charTable.put(new Character((char)913), "Alpha");
		charTable.put(new Character((char)914), "Beta");
		charTable.put(new Character((char)915), "Gamma");
		charTable.put(new Character((char)916), "Delta");
		charTable.put(new Character((char)917), "Epsilon");
		charTable.put(new Character((char)918), "Zeta");
		charTable.put(new Character((char)919), "Eta");
		charTable.put(new Character((char)920), "Theta");
		charTable.put(new Character((char)921), "Iota");
		charTable.put(new Character((char)922), "Kappa");
		charTable.put(new Character((char)923), "Lambda");
		charTable.put(new Character((char)924), "Mu");
		charTable.put(new Character((char)925), "Nu");
		charTable.put(new Character((char)926), "Xi");
		charTable.put(new Character((char)927), "Omicron");
		charTable.put(new Character((char)928), "Pi");
		charTable.put(new Character((char)929), "Rho");
		charTable.put(new Character((char)931), "Sigma");
		charTable.put(new Character((char)932), "Tau");
		charTable.put(new Character((char)933), "Upsilon");
		charTable.put(new Character((char)934), "Phi");
		charTable.put(new Character((char)935), "Chi");
		charTable.put(new Character((char)936), "Psi");
		charTable.put(new Character((char)937), "Omega");
		charTable.put(new Character((char)945), "alpha");
		charTable.put(new Character((char)946), "beta");
		charTable.put(new Character((char)947), "gamma");
		charTable.put(new Character((char)948), "delta");
		charTable.put(new Character((char)949), "epsilon");
		charTable.put(new Character((char)950), "zeta");
		charTable.put(new Character((char)951), "eta");
		charTable.put(new Character((char)952), "theta");
		charTable.put(new Character((char)953), "iota");
		charTable.put(new Character((char)954), "kappa");
		charTable.put(new Character((char)955), "lambda");
		charTable.put(new Character((char)956), "mu");
		charTable.put(new Character((char)957), "nu");
		charTable.put(new Character((char)958), "xi");
		charTable.put(new Character((char)959), "omicron");
		charTable.put(new Character((char)960), "pi");
		charTable.put(new Character((char)961), "rho");
		charTable.put(new Character((char)962), "sigmaf");
		charTable.put(new Character((char)963), "sigma");
		charTable.put(new Character((char)964), "tau");
		charTable.put(new Character((char)965), "upsilon");
		charTable.put(new Character((char)966), "phi");
		charTable.put(new Character((char)967), "chi");
		charTable.put(new Character((char)968), "psi");
		charTable.put(new Character((char)969), "omega");
		charTable.put(new Character((char)977), "thetasym");
		charTable.put(new Character((char)978), "upsih");
		charTable.put(new Character((char)982), "piv");
		charTable.put(new Character((char)8194), "ensp");
		charTable.put(new Character((char)8195), "emsp");
		charTable.put(new Character((char)8201), "thinsp");
		charTable.put(new Character((char)8204), "zwnj");
		charTable.put(new Character((char)8205), "zwj");
		charTable.put(new Character((char)8206), "lrm");
		charTable.put(new Character((char)8207), "rlm");
		charTable.put(new Character((char)8211), "ndash");
		charTable.put(new Character((char)8212), "mdash");
		charTable.put(new Character((char)8216), "lsquo");
		charTable.put(new Character((char)8217), "rsquo");
		charTable.put(new Character((char)8218), "sbquo");
		charTable.put(new Character((char)8220), "ldquo");
		charTable.put(new Character((char)8221), "rdquo");
		charTable.put(new Character((char)8222), "bdquo");
		charTable.put(new Character((char)8224), "dagger");
		charTable.put(new Character((char)8225), "Dagger");
		charTable.put(new Character((char)8226), "bull");
		charTable.put(new Character((char)8230), "hellip");
		charTable.put(new Character((char)8240), "permil");
		charTable.put(new Character((char)8242), "prime");
		charTable.put(new Character((char)8243), "Prime");
		charTable.put(new Character((char)8249), "lsaquo");
		charTable.put(new Character((char)8250), "rsaquo");
		charTable.put(new Character((char)8254), "oline");
		charTable.put(new Character((char)8260), "frasl");
		charTable.put(new Character((char)8364), "euro");
		charTable.put(new Character((char)8465), "image");
		charTable.put(new Character((char)8472), "weierp");
		charTable.put(new Character((char)8476), "real");
		charTable.put(new Character((char)8482), "trade");
		charTable.put(new Character((char)8501), "alefsym");
		charTable.put(new Character((char)8592), "larr");
		charTable.put(new Character((char)8593), "uarr");
		charTable.put(new Character((char)8594), "rarr");
		charTable.put(new Character((char)8595), "darr");
		charTable.put(new Character((char)8596), "harr");
		charTable.put(new Character((char)8629), "crarr");
		charTable.put(new Character((char)8656), "lArr");
		charTable.put(new Character((char)8657), "uArr");
		charTable.put(new Character((char)8658), "rArr");
		charTable.put(new Character((char)8659), "dArr");
		charTable.put(new Character((char)8660), "hArr");
		charTable.put(new Character((char)8704), "forall");
		charTable.put(new Character((char)8706), "part");
		charTable.put(new Character((char)8707), "exist");
		charTable.put(new Character((char)8709), "empty");
		charTable.put(new Character((char)8711), "nabla");
		charTable.put(new Character((char)8712), "isin");
		charTable.put(new Character((char)8713), "notin");
		charTable.put(new Character((char)8715), "ni");
		charTable.put(new Character((char)8719), "prod");
		charTable.put(new Character((char)8721), "sum");
		charTable.put(new Character((char)8722), "minus");
		charTable.put(new Character((char)8727), "lowast");
		charTable.put(new Character((char)8730), "radic");
		charTable.put(new Character((char)8733), "prop");
		charTable.put(new Character((char)8734), "infin");
		charTable.put(new Character((char)8736), "ang");
		charTable.put(new Character((char)8743), "and");
		charTable.put(new Character((char)8744), "or");
		charTable.put(new Character((char)8745), "cap");
		charTable.put(new Character((char)8746), "cup");
		charTable.put(new Character((char)8747), "int");
		charTable.put(new Character((char)8756), "there4");
		charTable.put(new Character((char)8764), "sim");
		charTable.put(new Character((char)8773), "cong");
		charTable.put(new Character((char)8776), "asymp");
		charTable.put(new Character((char)8800), "ne");
		charTable.put(new Character((char)8801), "equiv");
		charTable.put(new Character((char)8804), "le");
		charTable.put(new Character((char)8805), "ge");
		charTable.put(new Character((char)8834), "sub");
		charTable.put(new Character((char)8835), "sup");
		charTable.put(new Character((char)8836), "nsub");
		charTable.put(new Character((char)8838), "sube");
		charTable.put(new Character((char)8839), "supe");
		charTable.put(new Character((char)8853), "oplus");
		charTable.put(new Character((char)8855), "otimes");
		charTable.put(new Character((char)8869), "perp");
		charTable.put(new Character((char)8901), "sdot");
		charTable.put(new Character((char)8968), "lceil");
		charTable.put(new Character((char)8969), "rceil");
		charTable.put(new Character((char)8970), "lfloor");
		charTable.put(new Character((char)8971), "rfloor");
		charTable.put(new Character((char)9001), "lang");
		charTable.put(new Character((char)9002), "rang");
		charTable.put(new Character((char)9674), "loz");
		charTable.put(new Character((char)9824), "spades");
		charTable.put(new Character((char)9827), "clubs");
		charTable.put(new Character((char)9829), "hearts");
		charTable.put(new Character((char)9830), "diams");
	}
}
