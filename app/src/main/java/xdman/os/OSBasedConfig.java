package xdman.os;

public abstract class OSBasedConfig<T> {
    private final T windows;
    private final T mac;
    private final T linux;

    public OSBasedConfig(T windows, T mac, T linux) {
        this.windows = windows;
        this.mac = mac;
        this.linux = linux;
    }

    public T getWindows() {
        return windows;
    }

    public T getMac() {
        return mac;
    }

    public T getLinux() {
        return linux;
    }
}
