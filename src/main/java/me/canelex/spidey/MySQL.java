package me.canelex.spidey;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@SuppressWarnings("ConstantConditions")
public class MySQL
{
	private MySQL()
	{
		super();
	}

	private static final Logger LOG = LoggerFactory.getLogger(MySQL.class);

	private static Connection getConnection()
	{
		try
		{
			return DriverManager.getConnection("jdbc:mysql://localhost:3306/canelex", Secrets.USERNAME, Secrets.PASS);
		}
		catch (final SQLException e)
		{
			LOG.error("There was an error establishing the connection to the database!", e);
		}
		return null;
	}

	private static long getProperty(final long guildId, final String property)
	{
		if (!hasProperty(guildId, property))
			return 0;

		try (final var c = getConnection(); final var ps = c.prepareStatement("SELECT `" + property + "` FROM `guilds` WHERE `guild_id`=?;"))
		{
			ps.setLong(1, guildId);
			try (final var rs = ps.executeQuery())
			{
				rs.next();
				return rs.getLong(property);
			}
		}
		catch (final SQLException e)
		{
			LOG.error("There was an error while requesting the {} property for guild {}!", property, guildId, e);
		}
		return 0;
	}

	private static void setProperty(final long guildId, final long id, final String property)
	{
		try (final var c = getConnection(); final var ps = c.prepareStatement("REPLACE INTO `guilds` (`guild_id`, `channel_id`, `role_id`) VALUES (?, ?, ?);"))
		{
			ps.setLong(1, guildId);
			ps.setLong(2, (property.equals("channel_id") ? id : getChannel(guildId)));
			ps.setLong(3, (property.equals("role_id") ? id : getRole(guildId)));
			ps.executeUpdate();
		}
		catch (final SQLException e)
		{
			LOG.error("There was an error while upserting the {} property for guild {}!", property, guildId, e);
		}
	}

	private static void removeProperty(final long guildId, final String property)
	{
		if (!hasProperty(guildId, property))
			return;

		try (final var c = getConnection(); final var ps = c.prepareStatement("UPDATE `guilds` SET `" + property + "`=0 WHERE `guild_id`=?;"))
		{
			ps.setLong(1, guildId);
			ps.executeUpdate();
		}
		catch (final SQLException e)
		{
			LOG.error("There was an error while removing the {} property of guild {}!", property, guildId, e);
		}
	}

	@SuppressWarnings("BooleanMethodIsAlwaysInverted")
	private static boolean hasProperty(final long guildId, final String property)
	{
		try (final var c = getConnection(); final var ps = c.prepareStatement("SELECT SUM(`" + property + "` > 0) AS total FROM `guilds` WHERE `guild_id`=?;"))
		{
			ps.setLong(1, guildId);
			try (final var rs = ps.executeQuery())
			{
				rs.next();
				return rs.getInt("total") != 0;
			}
		}
		catch (final SQLException e)
		{
			LOG.error("There was an error while checking if guild {} has the {} property set!", guildId, property, e);
		}
		return false;
	}

	public static long getChannel(final long guildId)
	{
		return getProperty(guildId, "channel_id");
	}

	public static long getRole(final long guildId)
	{
		return getProperty(guildId, "role_id");
	}

	public static void setChannel(final long guildId, final long id)
	{
		setProperty(guildId, id, "channel_id");
	}

	public static void setRole(final long guildId, final long id)
	{
		setProperty(guildId, id, "role_id");
	}

	public static void removeChannel(final long guildId)
	{
		removeProperty(guildId, "channel_id");
	}

	public static void removeRole(final long guildId)
	{
		removeProperty(guildId, "role_id");
	}
}