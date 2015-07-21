public final class IndexedByteArray
{
    public byte[] array;
    public int index;
    
    
    public IndexedByteArray(byte[] array, int idx)
    {
        if (array == null)
           throw new NullPointerException("The array cannot be null");
        
        this.array = array;
        this.index = idx;
    }
}