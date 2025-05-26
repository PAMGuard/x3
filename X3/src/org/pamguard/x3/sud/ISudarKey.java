package org.pamguard.x3.sud;

public class ISudarKey
{
    public String key1;
    public String key2;
 
    /**
     * Constructor for the key
     * @param key1 - the 
     * @param key2
     */
    public ISudarKey(String key1, String key2)
    {
        this.key1 = key1.toLowerCase();
        this.key2 = key2.toLowerCase();
    }
 
    @Override
    public boolean equals(Object o)
    {
        if (this == o) {
            return true;
        }
 
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
 
        ISudarKey key = (ISudarKey) o;
        if (key1 != null ? !key1.equals(key.key1) : key.key1 != null) {
            return false;
        }
 
        if (key2 != null ? !key2.equals(key.key2) : key.key2 != null) {
            return false;
        }
 
        return true;
    }
 
    @Override
    public int hashCode()
    {
        int result = key1 != null ? key1.hashCode() : 0;
        result = 31 * result + (key2 != null ? key2.hashCode() : 0);
        return result;
    }
 
    @Override
    public String toString() {
        return "[" + key1 + ", " + key2 + "]";
    }
}
