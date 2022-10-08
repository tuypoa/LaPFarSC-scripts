package br.lapfarsc.moleculardynamics.dto;

public class IndexDTO {

	
	private Float x;
	private Float y;
	private Float z;
	
	public Float getX() {
		return x;
	}
	public void setX(Float x) {
		this.x = x;
	}
	public Float getY() {
		return y;
	}
	public void setY(Float y) {
		this.y = y;
	}
	public Float getZ() {
		return z;
	}
	public void setZ(Float z) {
		this.z = z;
	}

	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof IndexDTO){
			IndexDTO i = (IndexDTO) obj;
			return i.getX().equals(this.x) &&
					i.getY().equals(this.y) &&
					i.getZ().equals(this.z);			
		}
		return super.equals(obj);
	}
	
	@Override
	public String toString() {
		return this.x.toString()+";"+this.z.toString()+";"+this.z.toString();
	}
	
	
}
