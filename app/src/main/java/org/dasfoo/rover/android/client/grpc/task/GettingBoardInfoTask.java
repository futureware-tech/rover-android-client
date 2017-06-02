package org.dasfoo.rover.android.client.grpc.task;

import org.dasfoo.rover.server.AmbientLightRequest;
import org.dasfoo.rover.server.AmbientLightResponse;
import org.dasfoo.rover.server.BatteryPercentageRequest;
import org.dasfoo.rover.server.BatteryPercentageResponse;
import org.dasfoo.rover.server.RoverServiceGrpc;
import org.dasfoo.rover.server.TemperatureAndHumidityRequest;
import org.dasfoo.rover.server.TemperatureAndHumidityResponse;

import io.grpc.StatusRuntimeException;

/**
 * Created by Katarina Sheremet on 5/18/16 10:51 AM.
 */
public class GettingBoardInfoTask extends AbstractGrpcTaskExecutor {

    /**
     * Send a board info request to the rover and wait for result.
     *
     * @param stub gRPC
     * @return response as text
     */
    @Override
    public String execute(final RoverServiceGrpc.RoverServiceBlockingStub stub) {
        try {
            // Get battery percentage
            final BatteryPercentageRequest batteryPercentageRequest =
                    BatteryPercentageRequest.getDefaultInstance();
            BatteryPercentageResponse batteryPercentageResponse =
                    stub.getBatteryPercentage(batteryPercentageRequest);
            // Get light
            final AmbientLightRequest ambientLightRequest =
                    AmbientLightRequest.getDefaultInstance();
            AmbientLightResponse ambientLightResponse =
                    stub.getAmbientLight(ambientLightRequest);
            final TemperatureAndHumidityRequest temperatureAndHumidityRequest =
                    TemperatureAndHumidityRequest.getDefaultInstance();
            TemperatureAndHumidityResponse temperatureAndHumidityResponse =
                    stub.getTemperatureAndHumidity(temperatureAndHumidityRequest);
            // Create answer
            return String.format(
                    "Battery: %d\n" +
                            "Light: %d\n" +
                            "Temperature: %d\n" +
                            "Humidity: %d\n",
                    batteryPercentageResponse.getBattery(),
                    ambientLightResponse.getLight(),
                    temperatureAndHumidityResponse.getTemperature(),
                    temperatureAndHumidityResponse.getHumidity());
        } catch (StatusRuntimeException e) {
            switch (e.getStatus().getCode()) {
                case UNKNOWN:
                    return "Unknown error. Try later";
                default:
                    return e.getMessage();
            }
        }
    }
}
