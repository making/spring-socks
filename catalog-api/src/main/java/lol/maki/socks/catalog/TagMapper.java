package lol.maki.socks.catalog;

import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class TagMapper {
	private final JdbcTemplate jdbcTemplate;

	public TagMapper(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public List<Tag> findAll() {
		return this.jdbcTemplate.query("SELECT name FROM tag ORDER BY tag_id", (rs, i) -> Tag.valueOf(rs.getString("name")));
	}
}
