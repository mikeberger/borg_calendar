package net.sf.borg.common.util;



import java.io.ByteArrayInputStream;

import java.io.ByteArrayOutputStream;

import java.io.ObjectInputStream;

import java.io.ObjectOutputStream;



/**

 * Convenience class for retrieving preferences.

 */

public class Prefs

{

	public static String getPref( String name, String def )

	{

		return getPrefs().getPref(name,def);

	}

	

	public static void putPref( String name, String val )

	{

		getPrefs().putPref(name,val);

	}

	

	public static int getPref( String name, int def )

	{

		return getPrefs().getPref(name,def);

	}

	

	public static void putPref( String name, int val )

	{

		getPrefs().putPref(name,val);

	}

	

	public static byte[] getMemento()

	{

		byte[] result = null;

		try

		{

			ByteArrayOutputStream bostr = new ByteArrayOutputStream();

			ObjectOutputStream oostr = new ObjectOutputStream(bostr);

			boolean success = false;

			try

			{

				oostr.writeObject(getPrefs());

				success = true;

			}

			finally

			{

				oostr.close();

			}

			if (success)

				result=bostr.toByteArray();

		}

		catch (Throwable thw)

		{}

		return result;

	}

	

	public static void setMemento(byte[] data)

	{

		try

		{

			ObjectInputStream oistr =

				new ObjectInputStream

				(

					new ByteArrayInputStream(data)

				);

			try

			{

				IPrefs prefs = (MemPrefsImpl) oistr.readObject();

					// explicit downcast to MemPrefsImpl instead of

					// IPrefs to avoid a malicious exploit

				PrefsHome.getInstance().setPrefs(prefs);

			}

			finally

			{

				oistr.close();

			}

		}

		catch (Throwable thw)

		{}

	}



	// private //

	private static IPrefs getPrefs()

	{

		return PrefsHome.getInstance().getPrefs();

	}

	

	private Prefs()

	{}

}

