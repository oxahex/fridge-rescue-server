package team.rescue.notification.repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Repository
public class SseEmitterRepository {

	private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();

	public SseEmitter save(String id, SseEmitter sseEmitter) {
		emitters.put(id, sseEmitter);
		return sseEmitter;
	}

	public Optional<SseEmitter> findById(String id) {
		return Optional.ofNullable(emitters.get(id));
	}

	public void deleteById(String id) {
		emitters.remove(id);
	}
}
