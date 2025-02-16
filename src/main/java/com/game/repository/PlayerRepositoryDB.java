package com.game.repository;

import com.game.entity.Player;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.hibernate.query.NativeQuery;
import org.hibernate.query.Query;
import org.springframework.stereotype.Repository;

import javax.annotation.PreDestroy;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

@Repository(value = "db")
public class PlayerRepositoryDB implements IPlayerRepository {
    private final SessionFactory sessionFactory;

    public PlayerRepositoryDB() {
        Properties properties = new Properties();
        properties.put(Environment.DIALECT, "org.hibernate.dialect.MySQL8Dialect");
        properties.put(Environment.DRIVER, "com.p6spy.engine.spy.P6SpyDriver");
        properties.put(Environment.URL, "jdbc:p6spy:mysql://localhost:3306/rpg");
        properties.put(Environment.USER, "root");
        properties.put(Environment.PASS, "root");
        properties.put(Environment.HBM2DDL_AUTO, "update");

        sessionFactory = new Configuration()
                .addAnnotatedClass(Player.class)
                .addProperties(properties)
                .buildSessionFactory();
    }

    @Override
    public List<Player> getAll(int pageNumber, int pageSize) {
        Session session = sessionFactory.openSession();
        try (session) {
            NativeQuery<Player> nativeQuery = session.createNativeQuery("SELECT * FROM rpg.player p", Player.class);
            nativeQuery.setFirstResult(pageNumber * pageSize);
            nativeQuery.setMaxResults(pageSize);
            return nativeQuery.list();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int getAllCount() {
        Session session = sessionFactory.openSession();
        try (session) {
            Query<Long> playersCount = session.createNamedQuery("PLAYERS_COUNT", Long.class);
            return playersCount.uniqueResult().intValue();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Player save(Player player) {
        Session session = sessionFactory.openSession();
        Transaction tx = session.beginTransaction();
        try (session) {
            session.persist(player);
            tx.commit();
            return player;
        } catch (Exception e) {
            tx.rollback();
            throw new RuntimeException("Error with saving player " + player.getName(),e);
        }
    }

    @Override
    public Player update(Player player) {
        Session session = sessionFactory.openSession();
        Transaction tx = session.beginTransaction();
        try (session) {
            Player merge = (Player) session.merge(player);
            tx.commit();
            return merge;
        } catch (Exception e) {
            tx.rollback();
            throw new RuntimeException("Error with updating player with id " + player.getId(), e);
        }
    }

    @Override
    public Optional<Player> findById(long id) {
        Session session = sessionFactory.openSession();
        try (session) {
            return Optional.ofNullable(session.find(Player.class, id));
        } catch (Exception e) {
            throw new RuntimeException("Player with id " + id + " is not found", e);
        }
    }

    @Override
    public void delete(Player player) {
        Session session = sessionFactory.openSession();
        Transaction tx = session.beginTransaction();
        try (session) {
            session.remove(player);
            tx.commit();
        } catch (Exception e) {
            tx.rollback();
            throw new RuntimeException("Error with deleting player with id " + player.getId(),e);
        }
    }

    @PreDestroy
    public void beforeStop() {
        sessionFactory.close();
    }
}