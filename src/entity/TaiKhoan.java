package entity;

import java.util.Objects;

public class TaiKhoan {
	private String maTK;
	private String username;
	private String password;
	private String trangThai;
	private String quyen;

	public TaiKhoan() {
	}

	public TaiKhoan(String maTK, String username, String password, String trangThai, String quyen) {
		super();
		setMaTK(maTK);
		setUsername(username);
		setPassword(password);
		this.trangThai = trangThai;
		this.quyen = quyen;
	}

	public String getMaTK() {
		return maTK;
	}

	public void setMaTK(String maTK) {
		if (maTK == null || maTK.trim().isEmpty()) {
			throw new IllegalArgumentException("Mã tài khoản không được để trống!");
		}
		this.maTK = maTK;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		if (username == null || username.trim().isEmpty()) {
			throw new IllegalArgumentException("Tên đăng nhập (Username) không được để trống!");
		}
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		if (password == null || password.length() < 6) {
			throw new IllegalArgumentException("Mật khẩu phải có độ dài ít nhất từ 6 ký tự trở lên!");
		}
		this.password = password;
	}

	public String getTrangThai() {
		return trangThai;
	}

	public void setTrangThai(String trangThai) {
		this.trangThai = trangThai;
	}

	public String getQuyen() {
		return quyen;
	}

	public void setQuyen(String quyen) {
		this.quyen = quyen;
	}

	@Override
	public String toString() {
		return "TaiKhoan [maTK=" + maTK + ", username=" + username + ", password=" + password + ", trangThai="
				+ trangThai + ", quyen=" + quyen + "]";
	}

	@Override
	public int hashCode() {
		return Objects.hash(maTK);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TaiKhoan other = (TaiKhoan) obj;
		return Objects.equals(maTK, other.maTK);
	}

}
