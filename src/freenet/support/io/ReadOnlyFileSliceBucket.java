/*
  ReadOnlyFileSliceBucket.java / Freenet
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

package freenet.support.io;

import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;

import freenet.support.SimpleFieldSet;


/**
 * FIXME: implement a hash verifying version of this.
 */
public class ReadOnlyFileSliceBucket implements Bucket, SerializableToFieldSetBucket {

	private final File file;
	private final long startAt;
	private final long length;
	
	public ReadOnlyFileSliceBucket(File f, long startAt, long length) {
		this.file = f;
		this.startAt = startAt;
		this.length = length;
	}
	
    public ReadOnlyFileSliceBucket(SimpleFieldSet fs) throws CannotCreateFromFieldSetException {
   		String tmp = fs.get("Filename");
   		if(tmp == null) throw new CannotCreateFromFieldSetException("No filename");
   		this.file = new File(tmp);
   		tmp = fs.get("Length");
   		if(tmp == null) throw new CannotCreateFromFieldSetException("No length");
   		try {
   			length = Long.parseLong(tmp);
   		} catch (NumberFormatException e) {
   			throw new CannotCreateFromFieldSetException("Corrupt length "+tmp, e);
   		}
   		tmp = fs.get("Offset");
   		if(tmp == null) throw new CannotCreateFromFieldSetException("No offset");
   		try {
   			startAt = Long.parseLong(tmp);
   		} catch (NumberFormatException e) {
   			throw new CannotCreateFromFieldSetException("Corrupt offset "+tmp, e);
   		}
	}
    
	public OutputStream getOutputStream() throws IOException {
		throw new IOException("Bucket is read-only");
	}

	public InputStream getInputStream() throws IOException {
		return new MyInputStream();
	}

	public String getName() {
		return "ROFS:"+file.getAbsolutePath()+":"+startAt+":"+length;
	}

	public long size() {
		return length;
	}

	public boolean isReadOnly() {
		return true;
	}

	public void setReadOnly() {
		// Do nothing
	}

	class MyInputStream extends InputStream {

		private RandomAccessFile f;
		private long ptr; // relative to startAt
		
		MyInputStream() throws IOException {
			try {
				this.f = new RandomAccessFile(file,"r");
				f.seek(startAt);
				if(f.length() < (startAt+length))
					throw new ReadOnlyFileSliceBucketException("File truncated? Length "+f.length()+" but start at "+startAt+" for "+length+" bytes");
				ptr = 0;
			} catch (FileNotFoundException e) {
				throw new ReadOnlyFileSliceBucketException(e);
			}
		}
		
		public int read() throws IOException {
			if(ptr > length)
				throw new EOFException();
			int x = f.read();
			ptr++;
			return x;
		}
		
		public int read(byte[] buf, int offset, int len) throws IOException {
			if(ptr > length)
				throw new EOFException();
			len = (int) Math.min(len, length - ptr);
			int x = f.read(buf, offset, len);
			ptr += x;
			return x;
		}
		
		public int read(byte[] buf) throws IOException {
			return read(buf, 0, buf.length);
		}

		public void close() throws IOException {
			f.close();
		}
		
		public void finalize() {
			try {
				close();
			} catch (IOException e) {
				// Ignore
			}
		}
	}

	public class ReadOnlyFileSliceBucketException extends IOException {
		private static final long serialVersionUID = -1;
		
		public ReadOnlyFileSliceBucketException(FileNotFoundException e) {
			super("File not found: "+e.getMessage());
			initCause(e);
		}

		public ReadOnlyFileSliceBucketException(String string) {
			super(string);
		}
		
	}

	public void free() {
		// Do nothing
	}

	public SimpleFieldSet toFieldSet() {
		SimpleFieldSet fs = new SimpleFieldSet();
		fs.put("Type", "ReadOnlyFileSliceBucket");
		fs.put("Filename", file.toString());
		fs.put("Offset", startAt);
		fs.put("Length", length);
		return fs;
	}
	
}
