package ch.sheremet.dasfoo.rover.android.client.grpc.task;

import android.os.AsyncTask;
import android.text.TextUtils;

import java.util.concurrent.TimeUnit;

import dasfoo.grpc.roverserver.nano.RoverServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

/**
 * Created by Katarina Sheremet on 5/17/16 9:40 PM.
 */
public class GrpcTask extends AsyncTask<String, Void, String> {
    private ManagedChannel mChannel;
    private OnTaskCompleted listener;
    private RoverServiceGrpc.RoverServiceBlockingStub stub;

    public final RoverServiceGrpc.RoverServiceBlockingStub getStub() {
        return stub;
    }

    public GrpcTask(final OnTaskCompleted listener) {
        this.listener = listener;
    }

    @Override
    protected String doInBackground(final String... params) {
        int mPort = TextUtils.isEmpty(params[1]) ? 0 : Integer.valueOf(params[1]);
        mChannel = ManagedChannelBuilder.forAddress(params[0], mPort)
                .usePlaintext(true)
                .build();
        stub = RoverServiceGrpc.newBlockingStub(mChannel);
        return null;
    }

    @Override
    protected void onPostExecute(final String result) {
        try {
            mChannel.shutdown().awaitTermination(1, TimeUnit.SECONDS);
            listener.onTaskCompleted(result);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

    }
}
