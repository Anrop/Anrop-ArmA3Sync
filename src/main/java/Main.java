import static spark.Spark.*;

import com.google.gson.Gson;
import fr.soe.a3s.exception.LoadingException;
import fr.soe.a3s.service.RepositoryService;

public class Main {
    public static void main(String[] args) {
        RepositoryService repositoryService = new RepositoryService();
        try {
            repositoryService.readAll();
        } catch (LoadingException e) {
            e.printStackTrace();

            System.err.println("Failed to start");
            System.exit(1);
        }

        Gson gson = new Gson();

        get("/repositories", (req, res) -> repositoryService.getRepositories(), gson::toJson);
        get("/repositories/:name", (req, res) -> repositoryService.getRepository(req.params("name")), gson::toJson);
        get("/repositories/:name/events", (req, res) -> repositoryService.getEvents(req.params("name")), gson::toJson);
    }
}

