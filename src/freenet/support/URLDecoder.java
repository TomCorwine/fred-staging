/*
  URLDecoder.java / Freenet, Java Adaptive Network Client
  Copyright (C) Ian Clarke
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * The class contains a utility method for converting a 
 * <code>String</code> out of a MIME format called 
 * "<code>x-www-form-urlencoded</code>" format. 
 * <p>
 * To convert a <code>String</code>, each character is examined in turn:
 * <ul>
 * <li>The ASCII characters '<code>a</code>' through '<code>z</code>', 
 *     '<code>A</code>' through '<code>Z</code>', and '<code>0</code>' 
 *     through '<code>9</code>' remain the same. 
 * <li>The plus sign '<code>+</code>' is converted into a 
 *     space character '<code>&nbsp;</code>'. 
 * <li>The percent sign '<code>%</code>' must be followed by a 
 *     two-digit hexadecimal number, and is converted into the
 *     corresponding 8-bit character.
 * <li>The following "safe" characters [RFC 1738] are passed as is,
 *     if they appear:
 *     <code>$ - _ . + ! * ' ( ) ,</code>
 * <li>Anything else encountered, though strictly speaking illegal, 
 *     is passed as is.
 * </ul>
 *
 * @author <a href="http://www.doc.ic.ac.uk/~twh1/">Theodore Hong</a>
 **/

public class URLDecoder
{
    // test harness
    public static void main(String[] args) throws URLEncodedFormatException {
	for (int i = 0; i < args.length; i++) {
	    System.out.println(args[i] + " -> " + decode(args[i]));
	}
    }

    /**
     * Characters which will be passed unaltered.
     **/
    private static final String safeCharList = "$-_.+!*'(),";

    /**
	 * Translates a string out of x-www-form-urlencoded format.
	 *
	 * @param s String to be translated.
	 * @return the translated String.
	 *
	 **/
	public static String decode(String s) throws URLEncodedFormatException {
		if (s.length() == 0)
			return "";
		int len = s.length();
		ByteArrayOutputStream decodedBytes = new ByteArrayOutputStream();

		for (int i = 0; i < len; i++) {
			char c = s.charAt(i);
			if (Character.isLetterOrDigit(c))
				decodedBytes.write(c);
			else if (c == '+')
				decodedBytes.write(' ');
			else if (safeCharList.indexOf(c) != -1)
				decodedBytes.write(c);
			else if (c == '%') {
				if (i >= len - 2) {
					throw new URLEncodedFormatException(s);
				}
				char[] hexChars = new char[2];

				hexChars[0] = s.charAt(++i);
				hexChars[1] = s.charAt(++i);

				String hexval = new String(hexChars);
				try {
					long read = Fields.hexToLong(hexval);
					if (read == 0)
						throw new URLEncodedFormatException("Can't encode" + " 00");
					decodedBytes.write((int) read);
				} catch (NumberFormatException nfe) {
					throw new URLEncodedFormatException(s);
				}
			} else
				decodedBytes.write(c);
			// throw new URLEncodedFormatException(s);
		}
		try {
			decodedBytes.close();
			return new String(decodedBytes.toByteArray(), "utf-8");
		} catch (IOException ioe1) {
			/* if this throws something's wrong */
		}
		throw new URLEncodedFormatException(s);
	}

}
