package org.dasfoo.rover.android.client.grpc.task;

import org.dasfoo.rover.android.client.BuildConfig;
import org.dasfoo.rover.android.client.util.L;

import java.util.concurrent.TimeUnit;

import dasfoo.grpc.roverserver.nano.RoverServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Metadata;
import io.grpc.stub.MetadataUtils;


/**
 * Created by Katarina Sheremet on 5/24/16 5:09 PM.
 */
public class GrpcConnection {
    /**
     * It is used for logging.
     */
    private static final String TAG = L.tagFor(GrpcConnection.class);

    /**
     * Forms header.
     */
    private static Metadata.Key<String> authKey =
            Metadata.Key.of("authentication", Metadata.ASCII_STRING_MARSHALLER);

    private final String mHost;

    private final int mPort;

    /**
     * Attaches to header for validation user on server.
     */
    private final String mPassword;

    private ManagedChannel mChannel;

    private RoverServiceGrpc.RoverServiceBlockingStub mStub;

    /**
     * Constructor.
     *
     * @param host is for connection to the server
     * @param port is for connection to the server
     * @param password is for validating user
     */
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

    /**
     * Getters method for returning password.
     *
     * @return password
     */
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
                L.v(TAG, e.toString());
            }
            return Boolean.FALSE;
        }
    }
}
