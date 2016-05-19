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
    RoverServiceGrpc.RoverServiceBlockingStub stub;

    public GrpcTask(OnTaskCompleted listener) {
        this.listener = listener;
    }

    protected void establishConnection(String host, String port) {
        int mPort = TextUtils.isEmpty(port) ? 0 : Integer.valueOf(port);
        mChannel = ManagedChannelBuilder.forAddress(host, mPort)
                .usePlaintext(true)
                .build();
        stub = RoverServiceGrpc.newBlockingStub(mChannel);
    }

    @Override
    protected String doInBackground(String... params) {
        return null;
    }

    @Override
    protected void onPostExecute(String result) {
        try {
            mChannel.shutdown().awaitTermination(1, TimeUnit.SECONDS);
            listener.onTaskCompleted(result);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

    }
}
