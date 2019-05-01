package inf112.roborally.game.server;

import inf112.roborally.game.RoboRallyGame;
import inf112.roborally.game.objects.Position;
import inf112.roborally.game.player.ProgramCard;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.util.Stack;

import static java.util.Collections.shuffle;

public class ServerHandler extends SimpleChannelInboundHandler<String> {

    private final RoboRallyGame game;
    protected Stack<ProgramCard> returnedProgramCards;
    protected Stack<ProgramCard> stackOfProgramCards;

    public ServerHandler(RoboRallyGame game) {
        this.game = game;
        stackOfProgramCards = ProgramCard.makeProgramCardDeck();
        returnedProgramCards = new Stack<>();
    }

    public static final ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        channels.add(ctx.channel());
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) {
        Channel incoming = ctx.channel();
        for (Channel channel : channels) {
            channel.writeAndFlush("[SERVER] -  " + incoming.remoteAddress() + "has left\n");
        }
        channels.remove(ctx.channel());
    }


    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, String msg) throws Exception {
        Channel incoming = channelHandlerContext.channel();
        System.out.println(msg);
        for (Channel channel : channels) {
            if (channel != incoming)
                channel.writeAndFlush("[" + incoming.remoteAddress() + "] " + msg + "\n");
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        String packet = msg.toString();
        String[] split = packet.split(" ", 2);
        String header = split[0];

        switch (header) {
            case "HANDSHAKE":
                System.out.println("[SERVER] " + split[1] + " has connected!");
                game.playerNames.add(split[1]);

                for (Channel channel : channels) {
                    channel.writeAndFlush(split[1] + " has connected!" + "\r\n");
                }
                break;
            case "REQUEST_CARDS":
                String[] cardlimitAndName = split[1].split(" ");
                int cardLimit = Integer.parseInt(cardlimitAndName[0]);
                for (int i = 0; i < cardLimit; i++) {
                    String card = stackOfProgramCards.pop().toString();
                    if (stackOfProgramCards.isEmpty()) { // in case the game drags on and we run out of cards - Morten
                        reshuffleDeck();
                    }
                    for (Channel channel : channels) {
                        channel.writeAndFlush("RECEIVE_CARDS " + cardlimitAndName[1] + " " + card + "\r\n");
                    }
                }
                break;
            case "CARD":
                ProgramCard card = new ProgramCard(split[2], split[3], split[4]);
                returnedProgramCards.add(card);
                for (Channel channel :
                        channels) {
                    channel.writeAndFlush(split[0] + " " + split[1] + "\r\n");
                }
                break;
            default:
                for (Channel channel :
                        channels) {
                    channel.writeAndFlush(split[0] + " " + split[1] + "\r\n");
                }

                break;
        }
    }
    private void reshuffleDeck() {
        while (!returnedProgramCards.empty())
            stackOfProgramCards.push(returnedProgramCards.pop());
        shuffle(stackOfProgramCards);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
    }
}
