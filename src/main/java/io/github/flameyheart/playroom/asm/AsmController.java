package io.github.flameyheart.playroom.asm;

public class AsmController implements Runnable {
    @Override
    public void run() {
        new PlayroomAsm().run();
        try {
            Class<?> asmClass = Class.forName("io.github.flameyheart.playroom.asm.PlayroomClientAsm");
            Runnable asm = (Runnable) asmClass.getConstructor().newInstance();
            asm.run();
        } catch (ClassCastException | ReflectiveOperationException ignored) {}
    }
}
