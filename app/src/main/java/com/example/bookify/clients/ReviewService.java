package com.example.bookify.clients;

import com.example.bookify.model.Accommodation;
import com.example.bookify.model.AccommodationInsertDTO;
import com.example.bookify.model.CommentDTO;
import com.example.bookify.model.RatingDTO;
import com.example.bookify.model.ReviewDTO;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ReviewService {

    @Headers({
            "User-Agent: Mobile-Android",
            "Content-Type:application/json"
    })
    @POST("reviews/new-owner/{ownerId}")
    Call<ReviewDTO> addOwnerReview(@Path("ownerId") Long ownerId, @Body ReviewDTO reviewDTO);

    @Headers({
            "User-Agent: Mobile-Android",
            "Content-Type:application/json"
    })
    @POST("reviews/new-accommodation/{accommodationId}")
    Call<ReviewDTO> addAccommodationReview(@Path("accommodationId") Long accommodationId, @Body ReviewDTO reviewDTO);

    @Headers({
            "User-Agent: Mobile-Android",
            "Content-Type:application/json"
    })
    @GET("reviews/owner/{ownerId}")
    Call<List<CommentDTO>> getOwnerComments(@Path("ownerId") Long ownerId);

    @Headers({
            "User-Agent: Mobile-Android",
            "Content-Type:application/json"
    })
    @PUT("reviews/report/{reviewId}")
    Call<Long> reportComment(@Path("reviewId") Long reviewId);

//    getOwnerRating(ownerId: number): Observable<RatingDTO>{
//        return this.httpClient.get<RatingDTO>(environment.apiReview + "/owner/" + ownerId + "/rating");
//    }

    @Headers({
            "User-Agent: Mobile-Android",
            "Content-Type:application/json"
    })
    @GET("reviews/owner/{ownerId}/rating")
    Call<RatingDTO> getOwnerRating(@Path("ownerId") Long ownerId);

}
