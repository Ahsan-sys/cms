package net.cms.app.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.json.JSONObject;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GenericResponse {
    private String message="";
    private int status =1;
    private JSONObject data;

    public JSONObject rspToJson(){
        JSONObject obj = new JSONObject();
        obj.put("message",this.message);
        obj.put("status",this.status);
        if(this.data != null){
            obj.put("data",this.data);
        }
        return obj;
    }
}
