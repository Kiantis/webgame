package ki.webgame;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Quick methods for digest processing. (SHA1, MD5...)
 */
public class DigestUtils
{
	public static final String DIGEST_MD5 = "MD5";

	public static final String DIGEST_SHA1 = "SHA1";
    
    public static final String DIGEST_SHA512 = "SHA-512";
    
	public static String md5(String s)
	{
		return digest(s, DIGEST_MD5);
	}

	public static String sha1(String s)
	{
		return digest(s, DIGEST_SHA1);
	}

	public static String sha512(String s)
	{
		return digest(s, DIGEST_SHA512);
	}
    
	public static String digest(String source, String algorythm)
	{
		if (source == null || source.length() == 0)
		{
			return source;
		}

		try
		{
			StringBuilder result = new StringBuilder();
			MessageDigest md = MessageDigest.getInstance(algorythm);
			byte[] bytes = md.digest(source.getBytes("UTF-8"));
			for (byte b : bytes)
			{
				String bb = "0" + Integer.toHexString(b);
				result.append(bb.substring(bb.length()-2));
			}
			return result.toString();
		}
		catch (NoSuchAlgorithmException | UnsupportedEncodingException e)
		{
			return null;
		}
	}
}
