package com.topsoutherncoders.ServerPush;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.router.Route;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@Push
@SpringBootApplication
public class ServerPushApplication implements AppShellConfigurator {

	@Route
	public static class MainView extends VerticalLayout {

		public MainView(ClockService service) {
			var timeDisplay = new Paragraph();
			add(timeDisplay);
			addAttachListener(e -> {
				// the UI object is used to synchronize changes coming from non-UI event
				UI ui = e.getUI();
				var cleanup = service.registerClockChangeListener(newMessage ->
						ui.access(() -> {
							// In this block changes to UI objects are properly synced
							timeDisplay.setText(newMessage);
						}));
				// Cleanup when window is closed
				addDetachListener(e1 -> cleanup.run());
			});
		}
	}

	public static void main(String[] args) { SpringApplication.run(ServerPushApplication.class, args);}

	/**
	 * A trivial (non-Vaadin-dependenct) demo service periodically providing new
	 * data. Mimics something that a real world service you might already have.
	 * In Spring stack might be implemented with e.g. Project Reactor "Flux"
	 */
	@Service
	public static class ClockService {

		List<Consumer<String>> listeners = new ArrayList<>();

		public ClockService() {
			Executors.newSingleThreadScheduledExecutor()
					.scheduleWithFixedDelay(() -> {
						var newData = "Time here at backend: " + LocalDateTime.now();
						listeners.forEach(c -> c.accept(newData));
					}, 1, 1, TimeUnit.SECONDS);
		}

		/**
		 * Registers a listener for new data
		 *
		 * @param consumer the listener
		 * @return a handle to de-register the listener
		 */
		public Runnable registerClockChangeListener(Consumer<String> consumer) {
			listeners.add(consumer);
			// Return a handle to de-register
			return () -> listeners.remove(consumer);
		}
	}

}