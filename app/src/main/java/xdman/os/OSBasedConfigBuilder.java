package xdman.os;

public abstract class OSBasedConfigBuilder<T, K extends OSBasedConfig<?>> {
    protected T windows;
    protected T mac;
    protected T linux;

    public OSBasedConfigBuilder<T, K> windows(T windows) {
        this.windows = windows;
        return this;
    }

    public OSBasedConfigBuilder<T, K> mac(T mac) {
        this.mac = mac;
        return this;
    }

    public OSBasedConfigBuilder<T, K> linux(T linux) {
        this.linux = linux;
        return this;
    }

    public abstract K build();
}