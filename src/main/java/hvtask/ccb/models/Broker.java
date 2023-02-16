package hvtask.ccb.models;

import java.util.Objects;

public class Broker {
    private String name;
    private String hostname;
    private int port;

    public Broker(String name, String hostname, int port) {
        this.name = name;
        this.hostname = hostname;
        this.port = port;
    }

    public String getName() {
        return name;
    }

    public String getHostname() {
        return hostname;
    }

    public int getPort() {
        return port;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Broker broker = (Broker) o;
        return port == broker.port && Objects.equals(name, broker.name) && Objects.equals(hostname, broker.hostname);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, hostname, port);
    }
}
