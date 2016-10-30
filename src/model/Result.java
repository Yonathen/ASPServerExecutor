package model;

import com.google.gson.Gson;

public class Result {
	private String model;
	private String error;
	private Gson gson;
	
	public Result() {
		this.model="";
		this.error="";
		this.gson= new Gson();
	}
	
	public String toJson()
	{
		return gson.toJson(this);
	}

	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}

}
