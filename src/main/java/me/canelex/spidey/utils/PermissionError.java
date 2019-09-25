package me.canelex.spidey.utils;

public class PermissionError
{
	private PermissionError()
	{
		super();
	}

	private static final String MISSING_PERMS = ":no_entry: Action can't be completed due to missing permission **";

	public static String getErrorMessage(final String permName)
	{
		return MISSING_PERMS + permName + "**.";
	}
}