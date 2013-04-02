package org.chamois.snippet

import org.chamois.model.Repository
import scala.xml.NodeSeq
import org.chamois.model.User
import net.liftweb.common.Full

object Repositories {
  
  def show(repo:Repository)(n:NodeSeq): NodeSeq = {
    <h2>{repo.name}</h2> ++ (repo.documents.toList match {
      case Nil => <p>This repository is empty.</p>
      case documents =>
        <ul>
          {documents.map(doc => <li>{doc.name}</li>)}
        </ul>
    })
  }
  
  def listRepositories(n:NodeSeq): NodeSeq =
    <ul>
      {Repository.findAll.map(repo =>
        <li><a href={"/repository/" + repo.slug}>{repo.name}</a></li>)}
    </ul>
}