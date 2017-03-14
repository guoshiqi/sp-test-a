package wendu.spidersdk;

/**
 * Created by du on 17/3/14.
 */

public interface IPersistence {
    void save(String key, String value);
    String read(String key);
}
