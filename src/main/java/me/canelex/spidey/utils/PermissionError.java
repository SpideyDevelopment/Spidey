package me.canelex.spidey.utils;

import me.canelex.jda.api.Permission;

public class PermissionError
{
	private PermissionError()
	{
		super();
	}

	private static final String MISSING_PERMS = ":no_entry: Action can't be completed because you don't have **";

	public static String getErrorMessage(final Permission perm)
	{
		return MISSING_PERMS + perm.getName() + "** permission.";
	}
}