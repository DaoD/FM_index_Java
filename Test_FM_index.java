import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Scanner;

public class Test_FM_index {
	public static final int BUFFER_SIZE = 0x100000;
//	public static final int BUFFER_SIZE = 3;
	public static final int SA_SIZE = 0x200;
//	public static final int SA_SIZE = 1;
	public static final int FILE_SIZE = 0xA000000;
//	public static final int FILE_SIZE = 3;
	public static ArrayList<int[]> occ_list  = new ArrayList<int[]>();
	public static ArrayList<Integer> sa_list = new ArrayList<Integer>();
	public static int[] C = new int[4];
	
	public static void main(String args[]) throws IOException{
		File file = new File(FM_index.class.getClassLoader().getResource("file2.txt").getFile());
		long filelength = file.length();
		BufferedReader br = new BufferedReader(new FileReader(file));
		MappedByteBuffer mbb = new RandomAccessFile(file, "r").getChannel().map(FileChannel.MapMode.READ_ONLY, 11, filelength-12);
		String line = br.readLine();
		System.out.println(line.length());
//		byte[] ipm = new byte[(int)filelength-11];
//		for(int offset = 0; offset < filelength - 12; offset ++) {
//			byte b = mbb.get(offset);
//			if(offset >= 200600 && offset <= 200720) System.out.print((char)b);
//			ipm[offset] = b;
//		}
//		byte[] buf1 = ipm;
//		byte[] buf2 = new byte[buf1.length];
//		IndexedByteArray iba1 = new IndexedByteArray(buf1, 0);
//		IndexedByteArray iba2 = new IndexedByteArray(buf2, 0);
//		BWT bwt = new BWT();
//		long start = System.currentTimeMillis();
//		bwt.forward(iba1, iba2);
//		long end = System.currentTimeMillis();
//		String afterBWT = new String(buf2);
//		System.out.println((float)(end - start)/1000.0);
//		System.out.println("indexed finished!");
//		Scanner sc = new Scanner(System.in);
//		String p;
//		while((p = sc.next()).length() != 1) {
//			char c = p.charAt(p.length()-1);
//			int sp = C(afterBWT,c) + 1;
//			int ep = C(afterBWT,(char)(c+1));
//			int i = p.length() - 1;
//			while(sp <= ep && i >= 1){
//				c = p.charAt(i-1);
//				sp = LFC1(afterBWT,sp-1,c)+1;
//				ep = LFC1(afterBWT,ep,c);
//				i --;
//			}
			
//			for(int m = sp; m <= ep; m++) {
//				System.out.print(bwt.get_SA()[m-1] + " ");
//			}
//			for(int m = 0; m < afterBWT.length(); m++) {
//				System.out.print(LFC1(afterBWT,2,afterBWT.charAt(2)) + " ");
//			}
			
//			for(int m = 0; m < afterBWT.length(); m++) {
//				System.out.println(bwt.get_SA()[m]);
//			}
			
//			System.out.print(Occ1(afterBWT,'G',1) + " ");
//			System.out.print(Occ1(afterBWT,'G',2) + " ");
//			System.out.print(Occ1(afterBWT,'G',3) + " ");
//			System.out.print(Occ1(afterBWT,'G',4) + " ");
//			System.out.print(Occ1(afterBWT,'G',5) + " ");
//			System.out.print(Occ1(afterBWT,'G',6) + " ");
//			System.out.print(C(afterBWT,afterBWT.charAt(2)) + " ");
//			System.out.print(Occ1(afterBWT,afterBWT.charAt(2),2) + " ");
//			System.out.println(afterBWT.length());
//			System.out.println();
//			System.out.println(ep - sp + 1);	
//		}
	}
	
	private static int LFC1(String afterBWT, int i, char c) {
		return C(afterBWT,c) + Occ1(afterBWT,c,i);
	}
	
	private static int Occ1(String s, char c, int i) {
		int num = 0;
		for(int j = 0; j < i; j++) {
			if(s.charAt(j) == c) {
				num ++;
			}
		}
		return num;
	}
	
	private static int C(String s, char c) {
		int num = 0;
		for(int i = 0; i < s.length(); i++) {
			if(s.charAt(i) < c) {
				num ++;
			}
		}
		return num;
	}
	
}
