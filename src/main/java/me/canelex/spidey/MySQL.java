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
	private static final String GUILDS_STATEMENT = "SELECT *, COUNT(*) AS total FROM `guilds` WHERE `guild_id`=? LIMIT 1;";

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

	public static long getChannel(final long guildId)
	{
		if (!hasChannel(guildId))
			return 0;

		try (final var c = getConnection(); final var ps = c.prepareStatement(GUILDS_STATEMENT))
		{
			ps.setLong(1, guildId);
			try (final var rs = ps.executeQuery())
			{
				rs.next();
				return rs.getLong("channel_id");
			}
		}
		catch (final SQLException e)
		{
			LOG.error("There was an error while requesting the log channel id for guild {}!", guildId, e);
		}
		return 0;
	}

	public static void upsertChannel(final long guildId, final long channelId)
	{
		try (final var c = getConnection(); final var ps = c.prepareStatement("REPLACE INTO `guilds` (`guild_id`, `channel_id`) VALUES (?, ?);"))
		{
			ps.setLong(1, guildId);
			ps.setLong(2, channelId);
			ps.executeUpdate();
		}
		catch (final SQLException e)
		{
			LOG.error("There was an error while upserting the log channel id for guild {}!", guildId, e);
		}
	}

	public static void removeChannel(final long guildId)
	{
		if (!hasChannel(guildId))
			return;

		try (final var c = getConnection(); final var ps = c.prepareStatement("DELETE FROM `guilds` WHERE `guild_id`=?;"))
		{
			ps.setLong(1, guildId);
			ps.executeUpdate();
		}
		catch (final SQLException e)
		{
			LOG.error("There was an error while removing the log channel id of guild {}!", guildId, e);
		}
	}

	public static boolean hasChannel(final long guildId)
	{
		try (final var c = getConnection(); final var ps = c.prepareStatement(GUILDS_STATEMENT))
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
			LOG.error("There was an error while checking if guild {} has a log channel set!", guildId, e);
		}
		return false;
	}
}