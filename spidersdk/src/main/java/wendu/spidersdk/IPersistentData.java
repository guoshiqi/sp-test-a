package wendu.spidersdk;
/**
 * Created by du on 17/3/10.
 */


public interface IPersistentData {
     void save(String key, String value);
     String read(String key);
}
