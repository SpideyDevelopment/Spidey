package me.canelex.Spidey.commands;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import me.canelex.Spidey.objects.command.ICommand;
import me.canelex.Spidey.utils.API;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class JoindateCommand implements ICommand {
	
	final Locale locale = new Locale("en", "EN");  
	final Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Europe/London"));        	
	final SimpleDateFormat date = new SimpleDateFormat("EEEE, d.LLLL Y", locale);      
	final SimpleDateFormat time = new SimpleDateFormat("HH:mm:ss", locale); 	

	@Override
	public final boolean called(final GuildMessageReceivedEvent e) {
		
		return true;
		
	}

	@Override
	public final void action(final GuildMessageReceivedEvent e) {
		
		API.deleteMessage(e.getMessage());
		
    	if (e.getMessage().getMentionedUsers().isEmpty()) {
    		
    		cal.setTimeInMillis(e.getMember().getTimeJoined().toInstant().toEpochMilli()); 
    		final String joindate = date.format(cal.getTime()).toString();
    		final String jointime = time.format(cal.getTime()).toString();        		
    		API.sendPrivateMessageFormat(e.getAuthor(), "Date and time of joining to guild **%s**: **%s** | **%s** UTC", false, e.getGuild().getName(), joindate, jointime);
    		
    	}
    	
    	else {
    		
    		final List<User> mentioned = e.getMessage().getMentionedUsers();
    		
    		for (final User user : mentioned) {
    			
    			final Member member = API.getMember(e.getGuild(), user);
        		cal.setTimeInMillis(member.getTimeJoined().toInstant().toEpochMilli());
        		final String joindate = date.format(cal.getTime()).toString();
        		final String jointime = time.format(cal.getTime()).toString();            		
        		API.sendPrivateMessageFormat(e.getAuthor(), "(**" + member.getEffectiveName() + "**) " + "Date and time of joining to guild **%s**: **%s** | **%s** UTC", false,  e.getGuild().getName(), joindate, jointime);          		
    		
    		}
    		
    	}		
		
	}

	@Override
	public final String help() {
		
		return "Sends you PM containing joindate of you/mentioned user";
		
	}

	@Override
	public void executed(final boolean success, final GuildMessageReceivedEvent e) {
		
		return;
		
	}

}