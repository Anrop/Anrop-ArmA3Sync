import static spark.Spark.*;

import com.google.gson.Gson;
import fr.soe.a3s.controller.ObserverText;
import fr.soe.a3s.domain.repository.Events;
import fr.soe.a3s.dto.RepositoryDTO;
import fr.soe.a3s.exception.LoadingException;
import fr.soe.a3s.exception.repository.RepositoryException;
import fr.soe.a3s.service.RepositoryService;

import java.io.IOException;

public class Main {
    private static RepositoryService repositoryService;

    public static void main(String[] args) {
        repositoryService = new RepositoryService();
        try {
            repositoryService.readAll();
            restoreEvents();
        } catch (LoadingException e) {
            e.printStackTrace();

            System.err.println("Failed to start");
            System.exit(1);
        }

        repositoryService.getRepositoryBuilderDAO().addObserverText(new ObserverText() {
            @Override
            public void update(String s) {
                System.out.println(s);
            }
        });

        Gson gson = new Gson();

        after((request, response) -> {
            response.header("Content-Encoding", "gzip");
        });

        get("/repositories", (req, res) -> repositoryService.getRepositories(), gson::toJson);
        get("/repositories/:name", (req, res) -> repositoryService.getRepository(req.params("name")), gson::toJson);
        post("/repositories/:name/build", (req, res) -> buildRepository(req.params("name")), gson::toJson);
        get("/repositories/:name/events", (req, res) -> repositoryService.getEvents(req.params("name")), gson::toJson);
    }

    private static void restoreEvents() {
        for (RepositoryDTO repository : repositoryService.getRepositories()) {
            try {
                Events events = RepositoryService.getRepositoryDAO().readEvents(repository.getName());
                RepositoryService.getRepositoryDAO().getMap().get(repository.getName()).setEvents(events);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static Object buildRepository(String name) {
        try {
            repositoryService.buildRepository(name);
            return true;
        } catch (RepositoryException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }
}

