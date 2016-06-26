using UnityEngine;
using System.Collections;
using FluffyUnderware.Curvy;
using FluffyUnderware.Curvy.Controllers;

[AddComponentMenu("chasing/charactor/junction handler", 1)]
public class JunctionHandler : MonoBehaviour {

    public void UseRandomConnection(CurvySplineMoveEventArgs e)
    {
        // we need a SplineController as well as a connection to work with
        if (e.Sender is MoveController && e.ControlPoint.Connection)
        {
            CurvySplineSegment current = e.ControlPoint;
            MoveController controller = (MoveController)e.Sender;
            int direction = controller.Direction;

            // Find a new spline to follow:
            // Get all connected ControlPoints and check angle
            var others = e.ControlPoint.Connection.OtherControlPoints(current);
            // If it's smaller or equal 90°, consider the connected spline as a valid path to follow, otherwise remove it from the list
            for (int i = others.Count - 1; i >= 0; i--)
            {
                if (e.AngleTo(others[i]) > 90)
                    others.RemoveAt(i);
            }

            bool reachEnd = false;
            if (direction == 1 && current.IsLastVisibleControlPoint || direction == -1 && current.IsFirstVisibleControlPoint)
            {
                reachEnd = true;
            }

            int randomIndex = 0;
            if (!reachEnd)
                randomIndex = Random.Range(-1, others.Count);
            else 
                randomIndex = Random.Range(0, others.Count);

            if (randomIndex < 0) // don't follow another, but use FollowUp if present
            {
                if (current.FollowUp)
                    e.Follow(current.FollowUp, current.FollowUpHeading);    // Follow the connected spline
            }
            else
            {
                e.Follow(others[randomIndex]); // Follow the new spline
            }

            // Set the controller to use the new spline
            controller.Spline = e.Spline;
            controller.RelativePosition = e.TF;

        }
    }
}
