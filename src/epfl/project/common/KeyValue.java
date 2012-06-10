package epfl.project.common;

public interface KeyValue<KR extends Comparable<KR>, VR> {
	public KR getKey();
	public VR getValue();
}
