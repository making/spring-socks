package lol.maki.socks.catalog;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import static java.util.stream.Collectors.toUnmodifiableList;

@Repository
public class SockMapper {
	private final JdbcTemplate jdbcTemplate;

	private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

	private final Set<String> acceptedOrder = Set.of("sock_id", "name", "price");

	private final RowMapper<Sock> sockBuilderRowMapper = (rs, i) -> ImmutableSock.builder()
			.sockId(UUID.fromString(rs.getString("sock_id")))
			.name(rs.getString("name"))
			.description(rs.getString("description"))
			.price(new BigDecimal(rs.getString("price")))
			.count(rs.getInt("count"))
			.addImageUrl(rs.getString("image_url_1"), rs.getString("image_url_2"))
			.tags(Arrays.stream(Objects.requireNonNullElse(rs.getString("tag"), "").split(","))
					.map(Tag::valueOf)
					.collect(toUnmodifiableList()))
			.build();

	private static final String BASE_QUERY = "SELECT s.sock_id, s.name, description, price, count, image_url_1, image_url_2, GROUP_CONCAT(t.name) AS tag"
			+ " FROM sock AS s"
			+ " JOIN sock_tag AS st ON s.sock_id = st.sock_id"
			+ " JOIN tag AS t ON st.tag_id = t.tag_id";

	public SockMapper(JdbcTemplate jdbcTemplate, NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
		this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
	}

	public Optional<Sock> findOne(UUID sockId) {
		try {
			final Sock sock = this.jdbcTemplate.queryForObject(BASE_QUERY
					+ " GROUP BY s.name, s.price, s.sock_id"
					+ " HAVING s.sock_id = ?", this.sockBuilderRowMapper, sockId.toString());
			return Optional.ofNullable(sock);
		}
		catch (EmptyResultDataAccessException e) {
			return Optional.empty();
		}
	}

	public List<Sock> findSocks(List<Tag> tags, String order, int page, int size) {
		final MapSqlParameterSource source = new MapSqlParameterSource();
		final StringBuilder sql = new StringBuilder(BASE_QUERY);
		if (!CollectionUtils.isEmpty(tags)) {
			sql.append(" WHERE t.name IN (:tags)");
			source.addValue("tags", tags.stream().map(Tag::name).collect(toUnmodifiableList()));
		}
		sql
				.append(" GROUP BY s.name, s.price, s.sock_id");
		if (this.acceptedOrder.contains(order)) {
			sql
					.append(" ORDER BY ")
					.append("s.")
					.append(order);
		}
		if (page > 0 && size > 0) {
			sql
					.append(" LIMIT ")
					.append(size)
					.append(" OFFSET ")
					.append((page - 1) * size);
		}
		return this.namedParameterJdbcTemplate.query(sql.toString(), source, this.sockBuilderRowMapper);
	}

	public long countByTag(List<Tag> tags) {
		final MapSqlParameterSource source = new MapSqlParameterSource();
		final StringBuilder sql = new StringBuilder("SELECT COUNT(DISTINCT s.sock_id) FROM sock AS s");
		if (!CollectionUtils.isEmpty(tags)) {
			sql
					.append(" JOIN sock_tag AS st ON s.sock_id = st.sock_id")
					.append(" JOIN tag AS t ON st.tag_id = t.tag_id")
					.append(" WHERE t.name IN (:tags)");
			source.addValue("tags", tags.stream().map(Tag::name).collect(toUnmodifiableList()));
		}
		final Long count = this.namedParameterJdbcTemplate.queryForObject(sql.toString(), source, Long.class);
		return Objects.requireNonNullElse(count, 0L);
	}
}
