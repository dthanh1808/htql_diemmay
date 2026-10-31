package entity;

import java.util.UUID;

public record DanhMuc(UUID id, String ten, String moTa) {
    @Override public String toString() { return ten; }
}
