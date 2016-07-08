package ch.sheremet.dasfoo.rover.android.client.grpc.task;

import android.util.Log;

import java.util.concurrent.TimeUnit;

import ch.sheremet.dasfoo.rover.android.client.BuildConfig;
import dasfoo.grpc.roverserver.nano.RoverServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Metadata;
import io.grpc.stub.MetadataUtils;


/**
 * Created by Katarina Sheremet on 5/24/16 5:09 PM.
 */
public class GrpcConnection {
    private static final String TAG = GrpcConnection.class.getName();

    private final String mHost;

    private final int mPort;

    private final String mPassword;

    private ManagedChannel mChannel;

    private RoverServiceGrpc.RoverServiceBlockingStub mStub;

    private static Metadata.Key<String> authKey =
            Metadata.Key.of("authentication", Metadata.ASCII_STRING_MARSHALLER);



    public GrpcConnection(final String host, final int port, final String password) {
        this.mHost = host;
        this.mPort = port;
        this.mPassword = password;
        establishConnection();
    }

    public final int getPort() {
        return mPort;
    }

    public final String getHost() {
        return mHost;
    }

    public final String getPassword() {
        return mPassword;
    }

    private void establishConnection() {
        // Create header for stub
        Metadata headers = new Metadata();
        headers.put(authKey, mPassword);
        mChannel = ManagedChannelBuilder.forAddress(mHost, mPort).build();
        mStub = RoverServiceGrpc.newBlockingStub(mChannel);
        // Attach header to stub
        mStub = MetadataUtils.attachHeaders(mStub, headers);
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
