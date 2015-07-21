import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Scanner;

public class FM_index {
	public static final int BUFFER_SIZE = 0x100000;//索引间隔大小
	public static final int SA_SIZE = 0x200;//sa间隔大小
//	public static final int SA_SIZE = 100;//sa间隔大小
	public static final int FILE_SIZE = 0xA000000;//文件分段大小
//	public static final int FILE_SIZE = 0x9C40;
	public static final int FOUND_SIZE = 0x400;//可查询短串长度
	public static ArrayList<int[]> occ_list  = new ArrayList<int[]>();
	public static ArrayList<Integer> sa_list = new ArrayList<Integer>();
	public static int[] C = new int[4];
	
	public static void buildIndex(String filename, String indexname) {
		String s = filename;
		File file = new File(FM_index.class.getClassLoader().getResource(s).getFile());
		long filelength = file.length();
		try{
			MappedByteBuffer mbb;
			int filestart = 0;
			int fileend = 0;
			int times = 0;
			BufferedReader bf = new BufferedReader(new FileReader(file));
			String firstline = bf.readLine();
			int ignoresize = firstline.length() + 1;
			// 文件分段
			for(int fileoffset = FILE_SIZE + ignoresize; fileoffset < filelength + FILE_SIZE - 1; fileoffset += FILE_SIZE) {
				String bwtfile = indexname + "_output_bwt_"  + times + ".txt";
				String cfile = indexname + "_output_c_"  + times + ".txt";
				String occfile = indexname + "_output_occ_"  + times + ".txt";
				String safile = indexname + "_output_sa_"  + times + ".txt";
				File output_bwt = new File(bwtfile);
				File output_c = new File(cfile);
				File output_occ = new File(occfile);
				File output_sa = new File(safile);
				BufferedWriter bw1 = new BufferedWriter(new FileWriter(output_bwt));
				BufferedWriter bw2 = new BufferedWriter(new FileWriter(output_c));
				BufferedWriter bw3 = new BufferedWriter(new FileWriter(output_occ));
				BufferedWriter bw4 = new BufferedWriter(new FileWriter(output_sa));
				int ipm_length;
				// 第二段重复第一段末尾长度为待查字串长度-1的字串
				if(filelength - fileoffset - 1 >= 0) {
					filestart = fileoffset - FILE_SIZE;
					fileend = fileoffset;
					if(times == 0) {
						mbb = new RandomAccessFile(file, "r").getChannel().map(FileChannel.MapMode.READ_ONLY, filestart, FILE_SIZE);
						ipm_length = fileend - filestart;
					}
					else {
						mbb = new RandomAccessFile(file, "r").getChannel().map(FileChannel.MapMode.READ_ONLY, filestart - FOUND_SIZE + 1, FILE_SIZE + FOUND_SIZE - 1);
						ipm_length = fileend - filestart + FOUND_SIZE - 1;
					}
				}
				else {
					filestart = fileoffset - FILE_SIZE;
					fileend = (int)filelength - 1;
					if(times == 0) {
						mbb = new RandomAccessFile(file, "r").getChannel().map(FileChannel.MapMode.READ_ONLY, filestart, fileend - filestart);
						ipm_length = fileend - filestart;
					}
					else {
						mbb = new RandomAccessFile(file, "r").getChannel().map(FileChannel.MapMode.READ_ONLY, filestart - FOUND_SIZE + 1, fileend - filestart + FOUND_SIZE - 1);
						ipm_length = fileend - filestart + FOUND_SIZE - 1;
					}
				}
				int skipspace = 0;
				for(int offset = 0; offset < ipm_length; offset ++) {
					if((char)(mbb.get(offset)) == '\n') {
						skipspace ++;
					}
				}
				byte[] ipm = new byte[ipm_length-skipspace+1];
				for(int offset = 0, ipm_offset = 0; offset < ipm_length; offset ++) {
					if((char)(mbb.get(offset)) == '\n') {
						continue;
					}
					ipm[ipm_offset] = mbb.get(offset);	
					ipm_offset ++;
				}
				
//				for(int i = 0; i < 10; i++) {
//					System.out.print((char)ipm[i]);
//				}
//				System.out.println(filelength);
				
				byte[] buf1 = ipm;
				byte[] buf2 = new byte[buf1.length];           
				IndexedByteArray iba1 = new IndexedByteArray(buf1, 0);
				IndexedByteArray iba2 = new IndexedByteArray(buf2, 0);
				BWT bwt = new BWT();
				bwt.forward(iba1, iba2);
				int start = 0;
				int end = 0;
				// 建立索引，每1M建立一个索引
				for(int offset = BUFFER_SIZE; offset < buf1.length + BUFFER_SIZE; offset += BUFFER_SIZE) {
					if (buf1.length - offset >= 0) {  
						start = offset - BUFFER_SIZE;
						end = offset;
					}
					else {
						start = offset - BUFFER_SIZE;
						end = buf1.length;
					}
					int[] tmp_c_list = get_c_list(buf2, start, end);
					C[0] = 1;
					C[1] += tmp_c_list[1];
					C[2] += tmp_c_list[2];
					C[3] += tmp_c_list[3];
					int[] tmp_occ_list = get_occ_list(buf2,start,end);
					if(offset == BUFFER_SIZE) {
						occ_list.add(tmp_occ_list);
						for(int i = 0; i < 4; i++) {
							bw3.write(tmp_occ_list[i] + ",");
						}
						bw3.write("\n");
					}
					else {
						int[] tmp = new int[4];
						tmp[0] = occ_list.get(occ_list.size()-1)[0] + tmp_occ_list[0];
						tmp[1] = occ_list.get(occ_list.size()-1)[1] + tmp_occ_list[1];
						tmp[2] = occ_list.get(occ_list.size()-1)[2] + tmp_occ_list[2];
						tmp[3] = occ_list.get(occ_list.size()-1)[3] + tmp_occ_list[3];
						occ_list.add(tmp);
						for(int i = 0; i < 4; i++) {
							bw3.write(tmp[i] + ",");
						}
						bw3.write("\n");
					}
				}
				bw3.flush();
				C[1] ++;
				C[2] ++;
				C[3] ++;
				for(int i = 0; i < 4; i++) {
					bw2.write(C[i] + ",");
					C[i] = 0;
				}
				for(int i = SA_SIZE; i <= buf1.length; i += SA_SIZE) {
					bw4.write(bwt.get_SA()[i-1] + "\n");
				}
				times ++;
				String afterBWT = new String(buf2);
				bw1.write(afterBWT);
				bw1.flush();
				bw1.close();
				bw2.flush();
				bw2.close();
				bw3.close();
				bw4.flush();
				bw4.close();
			}
			System.out.println("indexed finished!");
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	// 从已准备好的文件中读取索引
	public static String getIndex(String indexname, int times) {
		try{
			String bwtfile = indexname + "_output_bwt_"  + times + ".txt";
			String cfile = indexname + "_output_c_"  + times + ".txt";
			String occfile = indexname + "_output_occ_"  + times + ".txt";
			String safile = indexname + "_output_sa_"  + times + ".txt";
			File input_bwt = new File(bwtfile);
			File input_c = new File(cfile);
			File input_occ = new File(occfile);
			File input_sa = new File(safile);
			long filelength = input_bwt.length();
			MappedByteBuffer mbb = new RandomAccessFile(input_bwt, "r").getChannel().map(FileChannel.MapMode.READ_ONLY, 0, filelength);
			BufferedReader br1 = new BufferedReader(new FileReader(input_sa));
			BufferedReader br2 = new BufferedReader(new FileReader(input_c));
			BufferedReader br3 = new BufferedReader(new FileReader(input_occ));
			String line = br2.readLine();
			String[] num = line.split(",");
			for(int i = 0; i < 4; i++) {
				C[i] = Integer.parseInt(num[i]);
			}
			sa_list.clear();
			occ_list.clear();
			while((line = br3.readLine()) != null) {
				num = line.split(",");
				int[] tmp = new int[4];
				for(int i = 0; i < 4; i++) {
					tmp[i] = Integer.parseInt(num[i]);
				}
				occ_list.add(tmp);
			}
			while((line = br1.readLine()) != null) {
				sa_list.add(Integer.parseInt(line));
			}
			byte[] ipm = new byte[(int)filelength];
			for(int offset = 0; offset < filelength; offset ++) {
				ipm[offset] = mbb.get(offset);
			}
			System.out.println("Get the Index!");
			return new String(ipm);
		}catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static void main(String[] args) throws IOException {
		//建立索引，buildIndex参数，第一个文件为1，第二个文件为2，第三个文件为3		
		long start = System.currentTimeMillis();
		buildIndex("hs_ref_GRCh38.p2_chr3_1.fa", "index1");
//		buildIndex("file3.txt", "index3");
		long end = System.currentTimeMillis();
		System.out.println("Times: " + ((float)(end - start) / 1000.0) + " s");
		
		System.out.println("You can input the String you want to find: ");
		Scanner sc = new Scanner(System.in);
		String p;
		while((p = sc.next()).length() != 1) {
			long find_begin = System.currentTimeMillis();
			int num = 0;
			//此处第一个文件为j < 1，第二个为j < 4,第三个为j < 6
			for(int j = 0; j < 1; j++) {
				//getIndex第二个参数需要配置，第一个文件配置为1，第二个文件配置为2，第三个文件配置为3
				int flag = 0;
				int position;
				String afterBWT = getIndex("index1",j);
				char c = p.charAt(p.length()-1);
				int sp = getint((char)(c)) <= 3 ? C[getint((char)(c))] + 1 : afterBWT.length() + 1;
				int ep = getint((char)(c+1)) <= 3 ? C[getint((char)(c+1))] : afterBWT.length();
				int i = p.length() - 1;
				while(sp <= ep && i >= 1){
					c = p.charAt(i-1);
					sp = LFC(afterBWT,sp-1,c)+1;
					ep = LFC(afterBWT,ep,c);
					i --;
				}
				System.out.println("In the " + (j + 1) + " part of the file, the position in big file is: ");
				for(int m = sp; m <= ep; m++) {
					if(j != 0 && SA(afterBWT,m-1) + p.length() <= FOUND_SIZE - 1) {
						flag ++;
						continue;
					}
					else position = SA(afterBWT,m-1) + j * FILE_SIZE - (j == 0 ? 0: FOUND_SIZE - 1);
					System.out.print(position + " ");
				}
				if(sp <= ep && flag == 0) System.out.println();
				num += ep - sp + 1 - flag;
			}
			System.out.println("Total number is: " + num);
			long find_end = System.currentTimeMillis();
			System.out.println("Times used: " + (float)(find_end - find_begin)/1000.0 + " s");
		}
		
	}

	// 将一段BUFFER_SIZE大小的Occ存放在list中
	private static int[] get_occ_list(byte[] ipm,int s, int e) {
		int[] tmp = new int[4];
		for(int i = s; i < e; i++) {
			if(ipm[i] == (byte)'A') tmp[0] ++;
			else if(ipm[i] == (byte)'C') tmp[1] ++;
			else if(ipm[i] == (byte)'G') tmp[2] ++;
			else if(ipm[i] == (byte)'T') tmp[3] ++;
		}
		return tmp;
	}

	// 将一段BUFFER_SIZE大小的C存放在list中
	private static int[] get_c_list(byte[] ipm, int s, int e) {
		int[] tmp = new int[4];
		for(int i = s; i < e; i++) {
			if(ipm[i] == (byte)'A') tmp[0] ++;
			else if(ipm[i] == (byte)'C') tmp[1] ++;
			else if(ipm[i] == (byte)'G') tmp[2] ++;
			else if(ipm[i] == (byte)'T') tmp[3] ++;
		}
		int p1 = tmp[0];
		tmp[0] = 0;
		int p2 = tmp[1];
		tmp[1] = p1 + tmp[0];
		p1 = tmp[2];
		tmp[2] = p2 + tmp[1];
		p2 = tmp[3];
		tmp[3] = p1 + tmp[2];
		return tmp;
	}
	
	// 查找下一个字符位置LFC
	private static int LFC(String afterBWT, int i, char c) {
		if(c == '\0') return 0;
		else return C[getint(c)] + Occ(afterBWT,c,i);
	}
	
	// 利用Occ-list编写的Occ函数
	private static int Occ(String s, char c, int i) {
		int num = 0;
		int times = (i-1) / BUFFER_SIZE;
		for(int j = times * BUFFER_SIZE; j < i; j++) {
			if(s.charAt(j) == c) {
				num ++;
			}
		}
		if(times == 0) return num;
		else return occ_list.get(times - 1)[getint(c)] + num;
	}
	
	// 查找子串位置所用SA
	private static int SA(String s, int n) {
		int times = (n + 1) / SA_SIZE;
		int remain = (n + 1) % SA_SIZE;
		int after = 0;
		int next = n + 1;
		char c;
		while(remain != 0) {
			c = s.charAt(next - 1);
			next = LFC(s, next - 1, c) + 1;
			remain = next % SA_SIZE;
			times = next / SA_SIZE;
			after ++;
		}
		int m = sa_list.get(times-1);
		return (m + after) % s.length();
	}
	
	// 判断字符编号
	private static int getint(char c) {
		if(c <= '\0') return 5;
		else if(c > '\0' && c <= 'A') return 0;
		else if(c > 'A' && c <= 'C') return 1;
		else if(c > 'C' && c <= 'G') return 2;
		else if(c > 'G' && c <= 'T') return 3;
		else return 4;
	}
}
