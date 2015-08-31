import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Ricky on 8/31/2015.
 */
public class ObjectListToObject {

    public Map<String, Object> listToObject(List<?> objects) throws MultipleMapIdsException {
        if(objects.isEmpty() || objects == null) {
            return new HashMap<String, Object>();
        }
        final Field[] fields = objects.get(0).getClass().getDeclaredFields();
        Field mapField = null;
        for(Field field : fields) {
            if(field.isAnnotationPresent(MapId.class)) {
                if(mapField == null){
                    mapField = field;
                } else {
                    throw new MultipleMapIdsException("Object can be labeled with only one MapId");
                }
            }
        }
        mapField.setAccessible(true);

        Map<String, Object> finalObject = new HashMap<String, Object>();
        try{
            for(Object obj : objects) {
                String mapName = String.valueOf(mapField.get(obj));
                addToMap(mapName, obj, finalObject);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return finalObject;
    }

    public Map<String, Object> addToMap(String value, Object obj, Map<String, Object> map){
        if(value.indexOf(".") == -1) {
            map.put(value, obj);
        } else {
            String mapValue = value.substring(0, value.indexOf("."));
            String newValue = value.replace(mapValue + ".", "");
            if(map.containsKey(mapValue)){
                if(map.get(mapValue) instanceof Map<?,?>) {
                    Map<String, Object> subMap = (Map<String, Object>) map.get(mapValue);
                    addToMap(newValue, obj, subMap);
                } else {
                    map.remove(mapValue);
                    Map<String, Object> newMap = new HashMap<String, Object>();
                    addToMap(newValue, obj, newMap);
                    map.put(mapValue, newMap);
                }
            } else {
                Map<String, Object> newMap = new HashMap<String, Object>();
                addToMap(newValue, obj, newMap);
                map.put(mapValue, newMap);
            }
        }
        return map;
    }
}
