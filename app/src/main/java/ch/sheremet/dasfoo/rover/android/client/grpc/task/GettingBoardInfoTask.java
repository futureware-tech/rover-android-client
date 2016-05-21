package ch.sheremet.dasfoo.rover.android.client.grpc.task;

import dasfoo.grpc.roverserver.nano.AmbientLightRequest;
import dasfoo.grpc.roverserver.nano.AmbientLightResponse;
import dasfoo.grpc.roverserver.nano.BatteryPercentageRequest;
import dasfoo.grpc.roverserver.nano.BatteryPercentageResponse;
import dasfoo.grpc.roverserver.nano.TemperatureAndHumidityRequest;
import dasfoo.grpc.roverserver.nano.TemperatureAndHumidityResponse;
import io.grpc.StatusRuntimeException;

/**
 * Created by Katarina Sheremet on 5/18/16 10:51 AM.
 */
public class GettingBoardInfoTask extends GrpcTask {

    public GettingBoardInfoTask(final OnTaskCompleted listener) {
        super(listener);
    }

    @Override
    protected final String doInBackground(final String... params) {
        super.doInBackground(params[0], params[1]);
        try {
            // Get battery percentage
            BatteryPercentageRequest batteryReq = new BatteryPercentageRequest();
            BatteryPercentageResponse batteryRes = getStub().getBatteryPercentage(batteryReq);
            // Get light
            AmbientLightRequest lightReq = new AmbientLightRequest();
            AmbientLightResponse lightRes = getStub().getAmbientLight(lightReq);
            TemperatureAndHumidityRequest tAndHReq = new TemperatureAndHumidityRequest();
            TemperatureAndHumidityResponse tAndHRes = getStub().getTemperatureAndHumidity(tAndHReq);
            // Create answer
            StringBuilder answer = new StringBuilder();
            answer.append("Battery: ").append(batteryRes.battery).append("\n");
            answer.append("Light: ").append(lightRes.light).append("\n");
            answer.append("Temperature: ").append(tAndHRes.temperature).append("\n");
            answer.append("Humidity: ").append(tAndHRes.humidity).append("\n");
            return answer.toString();
        } catch (StatusRuntimeException e) {
            switch (e.getStatus().getCode()) {
                case UNKNOWN:
                    return "Unknown error. Try later";
                default:
                    return e.getMessage();
            }
        } catch (Exception e) {
            return "Failed... : " + e.getMessage();
        }
    }
}
