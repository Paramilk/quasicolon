package dev.qixils.semicolon.variables.parsers.numbers;

import dev.qixils.semicolon.Semicolon;
import org.jetbrains.annotations.NotNull;

public final class ByteParser extends NumberParser<Byte> {
	public ByteParser(@NotNull Semicolon bot) {
		super(bot);
	}

	@Override
	public @NotNull Byte fromDatabase(@NotNull String value) throws NumberFormatException {
		return Byte.parseByte(value);
	}
}
