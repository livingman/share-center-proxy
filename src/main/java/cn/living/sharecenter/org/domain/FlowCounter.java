package cn.living.sharecenter.org.domain;
/**
 *@Author living
 *@Description //流量统计
 *@Date 20:12 2019/9/20
 *@Param
 *@return
 **/
public class FlowCounter {
	/**流量上传*/
	public final static int FLOW_UPLOAD=0;
	/**流量下载*/
	public final static int FLOW_DOWNLOAD=1;
	
	private long uploadBytes;
	
	private long downloadBytes;
	
	public long getUploadBytes() {
		return uploadBytes;
	}
	public void setUploadBytes(long uploadBytes) {
		this.uploadBytes = uploadBytes;
	}
	public long getDownloadBytes() {
		return downloadBytes;
	}
	public void setDownloadBytes(long downloadBytes) {
		this.downloadBytes = downloadBytes;
	}
	
	
}
