public class BWT 
{
    private int size;
    private int primaryIndex;
    private DivSufSort saAlgo;
    private int[] SA;
    
    public BWT()
    {
       this(0);
    }
   
    public BWT(int size)
    {
       if (size < 0)
          throw new IllegalArgumentException("Invalid size parameter (must be at least 0)");

       this.size = size;
    }

    public int getPrimaryIndex()
    {
       return this.primaryIndex;
    }

    public boolean setPrimaryIndex(int primaryIndex)
    {
       if (primaryIndex < 0)
          return false;

       this.primaryIndex = primaryIndex;
       return true;
    }

    public int size()
    {
       return this.size;
    }

    public boolean setSize(int size)
    {
       if (size < 0)
           return false;

       this.size = size;
       return true;
    }

    // 利用排序后的SA得到BWT后的字符串
    public boolean forward(IndexedByteArray src, IndexedByteArray dst)
    {
        final byte[] input = src.array;
        final byte[] output = dst.array;
        final int srcIdx = src.index;
        final int dstIdx = dst.index;
        final int count = (this.size == 0) ? input.length - srcIdx :  this.size;

        if (count < 2)
        {
           if (count == 1)
              output[dst.index++] = input[src.index++];

           return true;
        }
       
        if (this.saAlgo == null)
           this.saAlgo = new DivSufSort(); // 实例化后缀数组排序
        else
           this.saAlgo.reset();

        // 计算后缀数组
        final int[] sa = this.saAlgo.computeSuffixArray(input, srcIdx, count);
        final int srcIdx2 = srcIdx - 1;
        int i = 0;
       
        for (; i<count; i++) 
        {
          // 找到基准索引
           if (sa[i] == 0)
              break;

           output[dstIdx+i] = input[srcIdx2+sa[i]];
        }
        
        output[dstIdx+i] = input[srcIdx2+count];
        this.setPrimaryIndex(i);

        for (i++; i<count; i++) {
           output[dstIdx+i] = input[srcIdx2+sa[i]];
        }
        
        SA = new int[count];
        for(int j = 0; j < count; j++) {
        	SA[j] = sa[j];
        }         
        
        src.index += count;
        dst.index += count;
        return true;
    }

    public int[] get_SA() {
    	return SA;
    }
}