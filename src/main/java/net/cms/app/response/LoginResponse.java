package net.cms.app.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.json.JSONArray;
import org.json.JSONObject;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponse {
    private String message="";
    private int status =1;
    private JSONObject data;
    private JSONArray dataArray;

    private String accessToken="";
    private String refreshToken="";

    public JSONObject rspToJson(){
        JSONObject obj = new JSONObject();
        obj.put("message",this.message);
        obj.put("status",this.status);
        if(this.data != null){
            obj.put("data",this.data);
        }
        if(this.dataArray != null){
            obj.put("data",this.dataArray);
        }
        if(!accessToken.isEmpty()){
            obj.put("Access-Token",this.accessToken);
        }
        if(!accessToken.isEmpty()){
            obj.put("Refresh-Token",this.refreshToken);
        }
        return obj;
    }
}
