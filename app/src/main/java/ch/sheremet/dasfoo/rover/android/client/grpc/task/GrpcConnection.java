package ch.sheremet.dasfoo.rover.android.client.grpc.task;

import android.util.Log;

import java.util.concurrent.TimeUnit;

import ch.sheremet.dasfoo.rover.android.client.BuildConfig;
import dasfoo.grpc.roverserver.nano.RoverServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;


/**
 * Created by Katarina Sheremet on 5/24/16 5:09 PM.
 */
public class GrpcConnection {
    private static final String TAG = GrpcConnection.class.getName();

    private final String mHost;

    private final int mPort;

    private ManagedChannel mChannel;

    private RoverServiceGrpc.RoverServiceBlockingStub mStub;


    public GrpcConnection(final String host, final int port) {
        this.mHost = host;
        this.mPort = port;
        establishConnection();
    }

    public final int getPort() {
        return mPort;
    }

    public final String getHost() {
        return mHost;
    }

    private void establishConnection() {
        mChannel = ManagedChannelBuilder.forAddress(mHost, mPort).build();
        mStub = RoverServiceGrpc.newBlockingStub(mChannel);
    }

    public final RoverServiceGrpc.RoverServiceBlockingStub getStub() {
        return mStub;
    }

    public final boolean shutDownConnection() {
        try {
            mChannel.shutdown().awaitTermination(1, TimeUnit.SECONDS);
            return Boolean.TRUE;
        } catch (InterruptedException e) {
            if (BuildConfig.DEBUG) {
                Log.v(TAG, e.toString());
            }
            return Boolean.FALSE;
        }
    }
}
