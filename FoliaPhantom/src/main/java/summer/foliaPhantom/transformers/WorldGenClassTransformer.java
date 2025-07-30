package summer.foliaPhantom.transformers;

import org.objectweb.asm.*;
import summer.foliaPhantom.patchers.FoliaPatcher;

import java.util.logging.Level;

public class WorldGenClassTransformer {
    private final java.util.logging.Logger logger;

    public WorldGenClassTransformer(java.util.logging.Logger logger) {
        this.logger = logger;
    }

    public byte[] transform(byte[] originalBytes) {
        String className = "Unknown";
        try {
            ClassReader cr = new ClassReader(originalBytes);
            className = cr.getClassName();
            ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
            ClassVisitor cv = new WorldGenClassVisitor(cw);
            cr.accept(cv, ClassReader.EXPAND_FRAMES);
            return cw.toByteArray();
        } catch (Exception e) {
            logger.log(Level.WARNING, "[Phantom] Failed to transform class " + className + " for WorldGen. Returning original bytes.", e);
            return originalBytes;
        }
    }

    private static class WorldGenClassVisitor extends ClassVisitor {
        public WorldGenClassVisitor(ClassVisitor cv) { super(Opcodes.ASM9, cv); }
        @Override
        public MethodVisitor visitMethod(int access, String name, String desc, String sig, String[] ex) {
            return new WorldGenMethodVisitor(super.visitMethod(access, name, desc, sig, ex));
        }
    }

    private static class WorldGenMethodVisitor extends MethodVisitor {
        private static final String PATCHER_INTERNAL_NAME = Type.getInternalName(FoliaPatcher.class);

        public WorldGenMethodVisitor(MethodVisitor mv) { super(Opcodes.ASM9, mv); }
        @Override
        public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean isInterface) {
            if (owner.endsWith("Plugin") && "getDefaultWorldGenerator".equals(name) && "(Ljava/lang/String;Ljava/lang/String;)Lorg/bukkit/generator/ChunkGenerator;".equals(desc)) {
                String newDesc = "(Lorg/bukkit/plugin/Plugin;Ljava/lang/String;Ljava/lang/String;)Lorg/bukkit/generator/ChunkGenerator;";
                super.visitMethodInsn(Opcodes.INVOKESTATIC, PATCHER_INTERNAL_NAME, name, newDesc, false);
                return;
            }
            super.visitMethodInsn(opcode, owner, name, desc, isInterface);
        }
    }
}
