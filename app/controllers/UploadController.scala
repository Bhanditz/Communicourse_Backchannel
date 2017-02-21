package controllers

import java.io.File

import com.google.common.io.Files
import models.Upload
import models.Upload._
import play.api.Play
import play.api.libs.Files.TemporaryFile
import play.api.mvc.{Action, Controller, RequestHeader}
import play.mvc.Http.Response
import play.mvc.{Http, Result}
import play.api.Play.current
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Created by robertMueller on 21.02.17.
  */
class UploadController extends Controller
{

  def rename(directory: String, fName: String): (String, String) ={
    val file = new File(directory + fName)
    file.exists match {
      case true => {
        val extension = Files.getFileExtension(fName)
        val withoutExtension = Files.getNameWithoutExtension(fName)
        val regex = """([^(]*)\((\d+)\)""".r
        val newVersion: String = regex findFirstIn fName match {
          case Some(regex(pre, version)) => pre + "("+ (version.toInt+1)+ ")" + "." + extension
          case None => withoutExtension + "(0)." + extension
        }
        rename(directory, newVersion)
      }
      case false => (directory, fName)
    }
  }

  def upload =
//(f: (RequestHeader, Seq[Upload], String) => Result)
        Action.async(parse.multipartFormData){request =>
          request.session.get("username").map {username =>
            request.body.file("file").map { file =>
              val filename = file.filename
              val contentType = file.contentType
              val imagesFolder = "/images/"
              val documetsFolder = "/documents/"
              val imagesDirectory = Play.application.path + "/public/uploaded/images/"
              val documentsDirectory = Play.application.path + "/public/uploaded/documents/"
              val imgDirectory = new File(String.valueOf(imagesDirectory));
              if (!imgDirectory.exists()){
                imgDirectory.mkdirs()
              }
              val docDirectory = new File(String.valueOf(imagesDirectory));
              if (!docDirectory.exists()){
                docDirectory.mkdirs()
              }
              var (dir, newFilename) = ("","")
              if(filename.contains(".jpg") || filename.contains(".png") ||filename.contains(".gif")){
                var (dir, newFilename) = rename(imagesDirectory, filename)
                file.ref.moveTo(new File(dir + newFilename))
              } else {
                var (dir, newFilename) = rename(imagesDirectory, filename)
                file.ref.moveTo(new File(dir + newFilename))
              }

              for {
                uploadDb <- userUpload(username, dir + filename)
                uploadsList <- allUploads
              } yield Ok(views.html.upload(request, request.session.get("username").get, Nil, uploadsList))


            }.getOrElse(Future(Redirect(routes.Application.login())))

          }.getOrElse(Future(Redirect(routes.Application.login())))

        }



}

