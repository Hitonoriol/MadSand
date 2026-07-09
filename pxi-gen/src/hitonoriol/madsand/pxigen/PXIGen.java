package hitonoriol.madsand.pxigen;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.imageio.ImageIO;

public final class PXIGen {
	private static final int PALETTE_COLOR_COUNT = 255;

	public static void main(String[] args) throws Exception {
		var config = Config.parse(args);
		var pxiFiles = findPxiFiles(config.root());

		if (pxiFiles.isEmpty()) {
			System.out.println("No images to convert");
			return;
		}

		var palette = readPalette(config);
		int converted = 0;

		for (var pxiFile : pxiFiles) {
			convertFile(pxiFile, palette);
			converted++;
		}

		System.out.println("Converted " + converted + " images with palette: " + config.palette());
	}

	private static List<Path> findPxiFiles(Path root) throws IOException {
		if (!Files.exists(root)) {
			throw new IOException("PXI root does not exist: " + root);
		}

		try (Stream<Path> stream = Files.walk(root)) {
			return stream
				.filter(Files::isRegularFile)
				.filter(path -> path.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".pxi"))
				.sorted(Comparator.naturalOrder())
				.collect(Collectors.toList());
		}
	}

	private static int[] readPalette(Config config) throws IOException {
		if (!Files.isRegularFile(config.palette())) {
			throw new IOException("Palette does not exist: " + config.palette());
		}

		try (var input = new BufferedInputStream(Files.newInputStream(config.palette()))) {
			var palette = new int[PALETTE_COLOR_COUNT];

			for (int i = 0; i < palette.length; i++) {
				palette[i] = decodePaletteColor(readU32(input));
			}

			return palette;
		}
	}

	private static void convertFile(Path pxiFile, int[] palette) throws IOException {
		try (var input = new BufferedInputStream(Files.newInputStream(pxiFile))) {
			int width = readU16(input);
			int height = readU16(input);
			long pixelCount = (long) width * height;

			var pixels = input.readNBytes((int) pixelCount);

			if (pixels.length != pixelCount) {
				throw new IOException("Image is corrupted: " + pxiFile);
			}

			var image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
			int offset = 0;

			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {
					int paletteIndex = Byte.toUnsignedInt(pixels[offset++]);
					image.setRGB(x, y, palette[paletteIndex]);
				}
			}

			var outputFile = replaceExtension(pxiFile, ".png");
			Files.createDirectories(outputFile.getParent());
			ImageIO.write(image, "PNG", outputFile.toFile());
			System.out.println("Wrote " + outputFile);
		}
	}

	private static Path replaceExtension(Path path, String newExtension) {
		var fileName = path.getFileName().toString();
		int dot = fileName.lastIndexOf('.');
		String baseName = dot >= 0 ? fileName.substring(0, dot) : fileName;
		return path.resolveSibling(baseName + newExtension);
	}

	private static int readU16(InputStream input) throws IOException {
		int b0 = input.read();
		int b1 = input.read();

		if ((b0 | b1) < 0) {
			throw new EOFException("Failed to read U16");
		}

		return b0 | (b1 << 8);
	}

	private static int readU32(InputStream input) throws IOException {
		int b0 = input.read();
		int b1 = input.read();
		int b2 = input.read();
		int b3 = input.read();

		if ((b0 | b1 | b2 | b3) < 0) {
			throw new EOFException("Failed to read U32");
		}

		return b0 | (b1 << 8) | (b2 << 16) | (b3 << 24);
	}

	private static int decodePaletteColor(int rgba8888) {
		return ((rgba8888 & 0x000000FF) << 24) | ((rgba8888 >>> 8) & 0x00FFFFFF);
	}

	private record Config(Path root, Path palette) {
		private static Config parse(String[] args) {
			Path root = null;
			Path palette = null;

			for (int i = 0; i < args.length; i++) {
				switch (args[i]) {
					case "--root" -> root = Paths.get(getValue(args, ++i, "--root")).toAbsolutePath().normalize();
					case "--palette" -> palette = Paths.get(getValue(args, ++i, "--palette")).toAbsolutePath().normalize();
					case "--help" -> {
						System.out.println("Usage: pxi-gen --root <dir> --palette <file>");
						System.exit(0);
					}
					default -> throw new IllegalArgumentException("Unknown argument: " + args[i]);
				}
			}

			if (root == null) {
				throw new IllegalArgumentException("--root is required");
			}

			if (palette == null) {
				throw new IllegalArgumentException("--palette is required");
			}

			return new Config(root, palette);
		}

		private static String getValue(String[] args, int index, String option) {
			if (index >= args.length) {
				throw new IllegalArgumentException("No value for " + option);
			}

			return args[index];
		}
	}
}
