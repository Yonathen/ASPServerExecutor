package control;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import model.MyCallback;
import model.Option;
import model.Result;
import service.SolverClingo;
import service.SolverDLV;

@ServerEndpoint("/home")
public class SocketController {

	@OnOpen
	public void onOpen(Session session) {
		// Metodo eseguito all'apertura della connessione

	}

	@OnMessage
	public void onMessage(String message, Session session) { // Metodo eseguito alla ricezione di un messaggio, la stringa 'message'rappresenta il messaggio in formato json
		JsonParser parser = new JsonParser();
		JsonElement element = parser.parse(message);
		JsonObject object = element.getAsJsonObject();
		Gson gson = new Gson();
		Lock lock = new ReentrantLock();
		String engine = gson.fromJson(object.get("engine"), String.class);
		Type programsType = new TypeToken<ArrayList<String>>() {
		}.getType();
		ArrayList<String> programs = gson.fromJson(object.get("program"), programsType);
		Type optionType = new TypeToken<ArrayList<Option>>() {
		}.getType();
		ArrayList<Option> options = gson.fromJson(object.get("option"), optionType);
		Result result = new Result();
		switch (engine) {
		case "dlv":
			SolverDLV service = new SolverDLV(programs);
			if (service.checkOptionsDLV(options)) { // cotrolla che le opzioni ricevute siano nel formato corretto
				service.solveAsync(options, new MyCallback(session.getAsyncRemote(),lock));

			} else {
				result.setError("Sorry, these options aren't valid");
			}
			break;
		case "clingo":
			SolverClingo serviceClingo = new SolverClingo(programs);
			if (serviceClingo.checkOptionsClingo(options)) {
				serviceClingo.solveAsync(options, new MyCallback(session.getAsyncRemote(),lock));
			} else {
				result.setError("Sorry, these options aren't valid");
			}
			break;

		default:
			break;
		}
		if (result.getError().equals(""))
		result.setError("Wait, I'm thinking..");//restitusce questo 'messaggio' se ancora il solver non ha finito di eseguire il programma
		lock.lock();
		session.getAsyncRemote().sendText(result.toJson());
		lock.unlock();


	}

	@OnClose
	public void onClose(Session session) {
		// Metodo eseguito alla chiusura della connessione
	}

	@OnError
	public void onError(Throwable exception, Session session) {
		// Metodo eseguito in caso di errore
	}
}