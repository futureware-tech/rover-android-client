package org.dasfoo.rover.android.client.grpc.task;

import dasfoo.grpc.roverserver.nano.AmbientLightRequest;
import dasfoo.grpc.roverserver.nano.AmbientLightResponse;
import dasfoo.grpc.roverserver.nano.BatteryPercentageRequest;
import dasfoo.grpc.roverserver.nano.BatteryPercentageResponse;
import dasfoo.grpc.roverserver.nano.RoverServiceGrpc;
import dasfoo.grpc.roverserver.nano.TemperatureAndHumidityRequest;
import dasfoo.grpc.roverserver.nano.TemperatureAndHumidityResponse;
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
                    new BatteryPercentageRequest();
            BatteryPercentageResponse batteryPercentageResponse =
                    stub.getBatteryPercentage(batteryPercentageRequest);
            // Get light
            final AmbientLightRequest ambientLightRequest = new AmbientLightRequest();
            AmbientLightResponse ambientLightResponse =
                    stub.getAmbientLight(ambientLightRequest);
            final TemperatureAndHumidityRequest temperatureAndHumidityRequest =
                    new TemperatureAndHumidityRequest();
            TemperatureAndHumidityResponse temperatureAndHumidityResponse =
                    stub.getTemperatureAndHumidity(temperatureAndHumidityRequest);
            // Create answer
            return String.format(
                    "Battery: %d\n" +
                    "Light: %d\n" +
                    "Temperature: %d\n" +
                    "Humidity: %d\n",
                    batteryPercentageResponse.battery,
                    ambientLightResponse.light,
                    temperatureAndHumidityResponse.temperature,
                    temperatureAndHumidityResponse.humidity);
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
