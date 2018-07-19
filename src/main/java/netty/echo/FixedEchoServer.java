package netty.echo;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.FixedLengthFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

/**
 * <一句话描述>
 *
 * @author wangyang
 * @version [需求编号, 2018/7/19]
 * @see [相关类/方法]
 * @since [产品/模块版本]
 */
public class FixedEchoServer
{
    public void bind(int port)
    {

        //配置服务端nio线程组
        EventLoopGroup bossGrop = new NioEventLoopGroup();
        EventLoopGroup workerGrop = new NioEventLoopGroup();
        ServerBootstrap b = new ServerBootstrap();
        try
        {
            b.group(bossGrop, workerGrop)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 100)
                .handler(new LoggingHandler(LogLevel.INFO))
                .childHandler(new ChannelInitializer<SocketChannel>()
                {
                    @Override
                    protected void initChannel(SocketChannel socketChannel)
                        throws Exception
                    {
                        socketChannel.pipeline().addLast(new FixedLengthFrameDecoder(20));
                        socketChannel.pipeline().addLast(new StringDecoder());
                        socketChannel.pipeline().addLast(new ChannelInboundHandlerAdapter()
                        {
                            private int counter = 0;

                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg)
                                throws Exception
                            {
                                String request = (String)msg;
                                counter = request.length();
                                System.out.println("request [" + request + "]" + counter);
                                ByteBuf echo = Unpooled.copiedBuffer(request.getBytes());
                                ctx.writeAndFlush(echo);
                            }

                            @Override
                            public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
                                throws Exception
                            {
                                cause.printStackTrace();
                                ctx.close();
                            }
                        });
                    }
                });

            //绑定端口，等待成功
            ChannelFuture f = b.bind(port).sync();
            //等待服务端监听端口关闭
            f.channel().closeFuture().sync();
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();

        }
        finally
        {
            bossGrop.shutdownGracefully();
            workerGrop.shutdownGracefully();
        }
    }

    public static void main(String[] args)
    {
        int port = 8080;
        if (args != null && args.length > 1)
        {
            port = Integer.parseInt(args[0]);
        }
        new FixedEchoServer().bind(port);
    }
}
